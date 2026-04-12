package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.dto.OrderCheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.order.dto.RatingResponse;
import id.ac.ui.cs.advprog.order.exception.CannotCancelOrderException;
import id.ac.ui.cs.advprog.order.exception.CannotRateOrderException;
import id.ac.ui.cs.advprog.order.exception.DuplicateRatingException;
import id.ac.ui.cs.advprog.order.exception.InvalidStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.JastipperCannotBuySelfException;
import id.ac.ui.cs.advprog.order.exception.OrderException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.port.InventoryPort;
import id.ac.ui.cs.advprog.order.port.ProfilePort;
import id.ac.ui.cs.advprog.order.port.WalletPort;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.order.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for order management.
 * Handles all business logic related to orders and ratings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RatingRepository ratingRepository;
    private final InventoryPort inventoryPort;
    private final WalletPort walletPort;
    private final ProfilePort profilePort;
    private final OrderStatusTransitionPolicy statusTransitionPolicy;

    /**
     * Create a new order through checkout process.
     * Validates: Jastiper != Titiper, verifies stock and funds
     */
    @Override
    @Transactional
    public Order checkoutOrder(OrderCheckoutRequest request) {
        log.info("Processing checkout for titiper={}, productId={}", request.getTitiperUserId(), request.getProductId());

        if (request.getJastiperUserId() != null
                && profilePort.isSelfPurchase(request.getTitiperUserId(), request.getJastiperUserId())) {
            throw new JastipperCannotBuySelfException(request.getTitiperUserId());
        }

        if (!inventoryPort.hasSufficientStock(request.getProductId(), request.getQuantity())) {
            throw new OrderException("Insufficient stock for productId: " + request.getProductId());
        }

        OrderStatus initialStatus = OrderStatus.PENDING;
        BigDecimal totalPrice = request.getTotalPrice();
        if (totalPrice != null && totalPrice.signum() > 0) {
            if (!walletPort.hasSufficientBalance(request.getTitiperUserId(), totalPrice)) {
                throw new OrderException("Insufficient wallet balance for userId: " + request.getTitiperUserId());
            }
            initialStatus = OrderStatus.PAID;
        }

        inventoryPort.reserveStock(request.getProductId(), request.getQuantity());

        try {
            if (initialStatus == OrderStatus.PAID) {
                walletPort.deductBalance(request.getTitiperUserId(), totalPrice);
            }

            Order order = Order.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .titiperUserId(request.getTitiperUserId())
                    .jastiperUserId(request.getJastiperUserId())
                    .quantity(request.getQuantity())
                    .shippingAddress(request.getShippingAddress())
                    .totalPrice(totalPrice)
                    .status(initialStatus)
                    .build();

            Order savedOrder = orderRepository.save(order);
            log.info("Order created with id={} status={}", savedOrder.getId(), savedOrder.getStatus());
            return savedOrder;
        } catch (RuntimeException ex) {
            inventoryPort.releaseReservedStock(request.getProductId(), request.getQuantity());
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByTitiper(String titiperUserId) {
        return orderRepository.findByTitiperUserId(titiperUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByJastiper(String jastiperUserId) {
        return orderRepository.findByJastiperUserId(jastiperUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getActiveOrders() {
        return orderRepository.findActiveOrders();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Update order status with transition validation.
     * Ensures valid state transitions and prevents invalid state changes.
     */
    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();
        if (!canTransitionStatus(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus.toString(), newStatus.toString());
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Validate if a status transition is allowed.
     * Valid flows:
     * - PENDING -> PAID
     * - PAID -> PURCHASED
     * - PURCHASED -> SHIPPED
     * - SHIPPED -> COMPLETED
     * - Any status -> CANCELLED (with conditions)
     */
    @Override
    public boolean canTransitionStatus(OrderStatus fromStatus, OrderStatus toStatus) {
        return statusTransitionPolicy.canTransition(fromStatus, toStatus);
    }

    /**
     * Cancel an order and trigger refund to Titiper's wallet.
     * Orders can only be cancelled before they have been shipped.
     */
    @Override
    @Transactional
    public Order cancelOrder(Long orderId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new CannotCancelOrderException(orderId, currentStatus.toString());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request.getReason());

        Order cancelledOrder = orderRepository.save(order);

        inventoryPort.releaseReservedStock(order.getProductId(), order.getQuantity());
        if (order.getTotalPrice() != null && order.getTotalPrice().signum() > 0
                && (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.PURCHASED)) {
            walletPort.refundBalance(order.getTitiperUserId(), order.getTotalPrice());
        }

        return cancelledOrder;
    }

    /**
     * Submit a rating for a completed order.
     */
    @Override
    @Transactional
    public RatingResponse submitRating(Long orderId, RatingRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Can only rate completed orders
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CannotRateOrderException(orderId, order.getStatus().toString());
        }

        if (!ratingRepository.findByOrderId(orderId).isEmpty()) {
            throw new DuplicateRatingException(orderId);
        }

        Rating rating = Rating.builder()
                .orderId(orderId)
                .jastiperUserId(resolveJastiperId(request, order))
                .productId(resolveProductId(request, order))
                .ratingValue(request.getRatingValue())
                .review(request.getReview())
                .ratedByTitiper(true)
                .build();

        return convertToResponse(ratingRepository.save(rating));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByOrder(Long orderId) {
        return ratingRepository.findByOrderId(orderId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByJastiper(String jastiperUserId) {
        return ratingRepository.findByJastiperUserId(jastiperUserId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Double> getAverageRatingForJastiper(String jastiperUserId) {
        return ratingRepository.getAverageRatingForJastiper(jastiperUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Double> getAverageRatingForProduct(String productId) {
        return ratingRepository.getAverageRatingForProduct(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByProduct(String productId) {
        return ratingRepository.findByProductId(productId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Legacy method support for backward compatibility
    @Override
    @Transactional
    public Order createOrder(Order order) {
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        return orderRepository.save(order);
    }

    // Legacy method support for backward compatibility
    @Override
    @Transactional
    public Order updateStatus(Long id, String status) {
        try {
            return updateOrderStatus(id, OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    /**
     * Delete an order (only PENDING orders can be deleted)
     */
    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot delete order. Only PENDING orders can be deleted. Current status: " + order.getStatus()
            );
        }

        orderRepository.deleteById(orderId);
    }

    /**
     * Update order details (only allowed for PENDING or PAID orders)
     */
    @Override
    @Transactional
    public Order updateOrder(Long orderId, Order updateRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Only allow updates for PENDING or PAID orders
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException(
                    "Cannot update order. Only PENDING or PAID orders can be updated. Current status: " + order.getStatus()
            );
        }

        if (updateRequest.getProductName() != null && !updateRequest.getProductName().isBlank()) {
            order.setProductName(updateRequest.getProductName());
        }
        if (updateRequest.getQuantity() != null && updateRequest.getQuantity() > 0) {
            order.setQuantity(updateRequest.getQuantity());
        }
        if (updateRequest.getShippingAddress() != null && !updateRequest.getShippingAddress().isBlank()) {
            order.setShippingAddress(updateRequest.getShippingAddress());
        }
        if (updateRequest.getStatus() != null) {
            if (!canTransitionStatus(order.getStatus(), updateRequest.getStatus())) {
                throw new InvalidStatusTransitionException(
                        order.getStatus().toString(),
                        updateRequest.getStatus().toString()
                );
            }
            order.setStatus(updateRequest.getStatus());
        }

        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private String resolveJastiperId(RatingRequest request, Order order) {
        if (request.getJastiperUserId() != null && !request.getJastiperUserId().isBlank()) {
            return request.getJastiperUserId();
        }
        return order.getJastiperUserId();
    }

    private String resolveProductId(RatingRequest request, Order order) {
        if (request.getProductId() != null && !request.getProductId().isBlank()) {
            return request.getProductId();
        }
        return order.getProductId();
    }

    private RatingResponse convertToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .orderId(rating.getOrderId())
                .jastiperUserId(rating.getJastiperUserId())
                .productId(rating.getProductId())
                .ratingValue(rating.getRatingValue())
                .review(rating.getReview())
                .ratedByTitiper(rating.getRatedByTitiper())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}


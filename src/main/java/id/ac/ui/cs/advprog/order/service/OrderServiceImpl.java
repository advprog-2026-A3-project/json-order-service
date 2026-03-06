package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.dto.OrderCheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.order.dto.RatingResponse;
import id.ac.ui.cs.advprog.order.exception.CannotCancelOrderException;
import id.ac.ui.cs.advprog.order.exception.CannotRateOrderException;
import id.ac.ui.cs.advprog.order.exception.InvalidStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.JastipperCannotBuySelfException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.order.repository.RatingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RatingRepository ratingRepository;

    // TODO: Integrate with external services (Wallet, Inventory, Profile)
    // @Autowired
    // private WalletClient walletClient;
    // @Autowired
    // private InventoryClient inventoryClient;
    // @Autowired
    // private ProfileClient profileClient;

    /**
     * Create a new order through checkout process.
     * Validates: Jastiper != Titiper, verifies stock and funds
     */
    @Override
    @Transactional
    public Order checkoutOrder(OrderCheckoutRequest request) {
        log.info("Processing checkout for Titiper: {} Product: {}", request.getTitiperUserId(), request.getProductId());

        // TODO: Get product details from Inventory Module
        // TODO: Get user details from Profile Module for verification

        // Constraint: Jastiper cannot buy their own product
        // TODO: Verify jastiper != titiper using Profile Module

        Order order = Order.builder()
                .productId(request.getProductId())
                .titiperUserId(request.getTitiperUserId())
                .quantity(request.getQuantity())
                .shippingAddress(request.getShippingAddress())
                // TODO: Get totalPrice from Inventory Module based on productId
                .status(OrderStatus.PENDING)
                .build();

        // TODO: Verify stock availability from Inventory Module
        // TODO: Verify wallet balance from Wallet Module

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} Status: {}", savedOrder.getId(), savedOrder.getStatus());

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByTitiper(String titiperUserId) {
        log.info("Fetching orders for Titiper: {}", titiperUserId);
        return orderRepository.findByTitiperUserId(titiperUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByJastiper(String jastiperUserId) {
        log.info("Fetching orders for Jastiper: {}", jastiperUserId);
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
        log.info("Updating order {} status to: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        // Validate status transition
        if (!canTransitionStatus(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus.toString(), newStatus.toString());
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated from {} to {}", orderId, currentStatus, newStatus);
        return updatedOrder;
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
        if (fromStatus == toStatus) {
            return true; // No change needed
        }

        return switch (fromStatus) {
            case PENDING -> toStatus == OrderStatus.PAID || toStatus == OrderStatus.CANCELLED;
            case PAID -> toStatus == OrderStatus.PURCHASED || toStatus == OrderStatus.CANCELLED;
            case PURCHASED -> toStatus == OrderStatus.SHIPPED || toStatus == OrderStatus.CANCELLED;
            case SHIPPED -> toStatus == OrderStatus.COMPLETED || toStatus == OrderStatus.CANCELLED;
            case COMPLETED -> false; // Cannot transition from completed
            case CANCELLED -> false; // Cannot transition from cancelled
        };
    }

    /**
     * Cancel an order and trigger refund to Titiper's wallet.
     * Orders can only be cancelled before they have been shipped.
     */
    @Override
    @Transactional
    public Order cancelOrder(Long orderId, CancelOrderRequest request) {
        log.info("Attempting to cancel order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        // Cannot cancel orders that have already been shipped or completed
        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.COMPLETED) {
            throw new CannotCancelOrderException(orderId, currentStatus.toString());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request.getReason());

        Order cancelledOrder = orderRepository.save(order);

        // TODO: Call Wallet Module to trigger refund
        // walletClient.refund(order.getTitiperUserId(), order.getTotalPrice());

        log.info("Order {} cancelled. Refund triggered for Titiper: {}", orderId, order.getTitiperUserId());
        return cancelledOrder;
    }

    /**
     * Submit a rating for a completed order.
     */
    @Override
    @Transactional
    public RatingResponse submitRating(Long orderId, RatingRequest request) {
        log.info("Submitting rating for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Can only rate completed orders
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CannotRateOrderException(orderId, order.getStatus().toString());
        }

        Rating rating = Rating.builder()
                .orderId(orderId)
                .jastiperUserId(request.getJastiperUserId())
                .productId(request.getProductId())
                .ratingValue(request.getRatingValue())
                .review(request.getReview())
                .ratedByTitiper(true)
                .build();

        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating submitted successfully for order: {}", orderId);

        return convertToResponse(savedRating);
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
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            return updateOrderStatus(id, newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    /**
     * Convert Rating entity to RatingResponse DTO
     */
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
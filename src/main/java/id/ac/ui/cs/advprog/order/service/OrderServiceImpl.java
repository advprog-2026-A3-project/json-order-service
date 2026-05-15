package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.client.InventoryClient;
import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.dto.inventory.InventoryProductResponse;
import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    // BARU(Order - Inventory): 1. Menambahkan InventoryClient untuk fetch product dari Inventory Service
    private final InventoryClient inventoryClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            InventoryClient inventoryClient
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;

        // BARU(Order - Inventory): 2. Inject InventoryClient melalui constructor
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // BARU(Order - Inventory): 3. Ambil dan override data product dari Inventory sebelum order dibuat
        enrichRequestWithInventoryProductData(request);

        validateNotSelfPurchase(request);

        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Order getOrderById(Long id) {
        return findExistingOrder(id);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderCreateRequest request) {
        Order existingOrder = findExistingOrder(id);

        if (existingOrder.getStatus() == OrderStatus.CANCELLED
                || existingOrder.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusTransitionException(
                    existingOrder.getStatus(),
                    existingOrder.getStatus()
            );
        }

        // BARU(Order - Inventory): 4. Saat update order, data product juga tetap diambil dari Inventory
        enrichRequestWithInventoryProductData(request);

        validateNotSelfPurchase(request);

        orderMapper.copyToExisting(request, existingOrder);

        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order existingOrder = findExistingOrder(id);
        OrderStatus currentStatus = existingOrder.getStatus();

        if (!canTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, newStatus);
        }

        existingOrder.setStatus(newStatus);

        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public Order cancelOrderById(Long id) {
        Order existingOrder = findExistingOrder(id);
        OrderStatus currentStatus = existingOrder.getStatus();

        if (!canCancel(currentStatus)) {
            throw new InvalidOrderStatusTransitionException(
                    currentStatus,
                    OrderStatus.CANCELLED
            );
        }

        existingOrder.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(existingOrder);
    }

    // BARU(Order - Inventory): 5. Fetch product, validasi stok, lalu override productName, jastiperUserId, dan totalPrice
    private void enrichRequestWithInventoryProductData(OrderCreateRequest request) {
        InventoryProductResponse product =
                inventoryClient.getProductById(request.getProductId());

        validateStock(product, request.getQuantity());

        BigDecimal totalPrice = product.getHarga()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        request.setProductName(product.getNama());
        request.setJastiperUserId(product.getJastiperId());
        request.setTotalPrice(totalPrice);
    }

    // BARU(Order - Inventory): 6. Validasi quantity dan stok berdasarkan data Inventory
    private void validateStock(InventoryProductResponse product, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1"
            );
        }

        if (product.getStok() == null || product.getStok() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient product stock"
            );
        }
    }

    private boolean canTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return true;
        }

        return switch (current) {
            case PENDING -> next == OrderStatus.PAID
                    || next == OrderStatus.CANCELLED
                    || next == OrderStatus.CHECKOUT_PENDING;
            case CHECKOUT_PENDING -> next == OrderStatus.PAID
                    || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.PURCHASED
                    || next == OrderStatus.CANCELLED;
            case PURCHASED -> next == OrderStatus.SHIPPED
                    || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private boolean canCancel(OrderStatus currentStatus) {
        return currentStatus == OrderStatus.PENDING
                || currentStatus == OrderStatus.CHECKOUT_PENDING
                || currentStatus == OrderStatus.PAID
                || currentStatus == OrderStatus.PURCHASED;
    }

    private void validateNotSelfPurchase(OrderCreateRequest request) {
        String titiperId = request.getTitiperUserId();
        String jastiperId = request.getJastiperUserId();

        if (titiperId != null
                && !titiperId.isBlank()
                && titiperId.equals(jastiperId)) {
            throw new SelfPurchaseNotAllowedException();
        }
    }

    private Order findExistingOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
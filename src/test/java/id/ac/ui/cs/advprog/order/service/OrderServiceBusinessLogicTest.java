package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.exception.CannotCancelOrderException;
import id.ac.ui.cs.advprog.order.exception.InvalidStatusTransitionException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.port.InventoryPort;
import id.ac.ui.cs.advprog.order.port.ProfilePort;
import id.ac.ui.cs.advprog.order.port.WalletPort;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.order.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Comprehensive Unit Tests for Business Logic
 * Tests: Status transitions, cancellation, rating, query methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service - Business Logic Tests")
class OrderServiceBusinessLogicTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private WalletPort walletPort;

    @Mock
    private ProfilePort profilePort;

    @Spy
    private OrderStatusTransitionPolicy statusTransitionPolicy;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .productName("Limited Sushi")
                .titiperUserId("titiper-001")
                .jastiperUserId("jastiper-001")
                .quantity(2)
                .shippingAddress("Depok, Indonesia")
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lenient().when(inventoryPort.hasSufficientStock(anyString(), anyInt())).thenReturn(true);
        lenient().when(walletPort.hasSufficientBalance(anyString(), any(BigDecimal.class))).thenReturn(true);
        lenient().when(profilePort.isSelfPurchase(anyString(), anyString())).thenReturn(false);
    }

    // ========== Status Transition Tests ==========

    @Test
    @DisplayName("Should allow PENDING to PAID transition")
    void testCanTransition_PendingToPaid() {
        boolean result = orderService.canTransitionStatus(OrderStatus.PENDING, OrderStatus.PAID);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should allow PAID to PURCHASED transition")
    void testCanTransition_PaidToPurchased() {
        boolean result = orderService.canTransitionStatus(OrderStatus.PAID, OrderStatus.PURCHASED);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should allow PURCHASED to SHIPPED transition")
    void testCanTransition_PurchasedToShipped() {
        boolean result = orderService.canTransitionStatus(OrderStatus.PURCHASED, OrderStatus.SHIPPED);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should allow SHIPPED to COMPLETED transition")
    void testCanTransition_ShippedToCompleted() {
        boolean result = orderService.canTransitionStatus(OrderStatus.SHIPPED, OrderStatus.COMPLETED);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should NOT allow invalid status transitions")
    void testCanTransition_InvalidTransition() {
        boolean result = orderService.canTransitionStatus(OrderStatus.COMPLETED, OrderStatus.PENDING);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should NOT allow COMPLETED to SHIPPED transition")
    void testCanTransition_CompletedToShipped() {
        boolean result = orderService.canTransitionStatus(OrderStatus.COMPLETED, OrderStatus.SHIPPED);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should NOT allow SHIPPED to CANCELLED transition")
    void testCanTransition_ShippedToCancelled() {
        boolean result = orderService.canTransitionStatus(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
        assertFalse(result);
    }

    // ========== Cancellation Tests ==========

    @Test
    @DisplayName("Should cancel PENDING order")
    void testCancelOrder_PendingOrder_Success() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Change of mind")
                .build();

        Order result = orderService.cancelOrder(1L, request);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should NOT allow cancellation of COMPLETED order")
    void testCancelOrder_CompletedOrder_Fails() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Change of mind")
                .build();

        assertThrows(CannotCancelOrderException.class, () -> orderService.cancelOrder(1L, request));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should NOT allow cancellation of SHIPPED order")
    void testCancelOrder_ShippedOrder_Fails() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Change of mind")
                .build();

        assertThrows(CannotCancelOrderException.class, () -> orderService.cancelOrder(1L, request));

        verify(orderRepository, never()).save(any(Order.class));
    }

    // ========== Query Methods Tests ==========

    @Test
    @DisplayName("Should find all orders by Titiper")
    void testGetOrdersByTitiper() {
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByTitiperUserId("titiper-001")).thenReturn(orders);

        List<Order> result = orderService.getOrdersByTitiper("titiper-001");

        assertEquals(1, result.size());
        assertEquals("titiper-001", result.getFirst().getTitiperUserId());
        verify(orderRepository, times(1)).findByTitiperUserId("titiper-001");
    }

    @Test
    @DisplayName("Should find all orders by Jastiper")
    void testGetOrdersByJastiper() {
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByJastiperUserId("jastiper-001")).thenReturn(orders);

        List<Order> result = orderService.getOrdersByJastiper("jastiper-001");

        assertEquals(1, result.size());
        assertEquals("jastiper-001", result.getFirst().getJastiperUserId());
        verify(orderRepository, times(1)).findByJastiperUserId("jastiper-001");
    }

    @Test
    @DisplayName("Should find all orders by status")
    void testGetOrdersByStatus() {
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.getFirst().getStatus());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should find all active orders")
    void testGetActiveOrders() {
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findActiveOrders()).thenReturn(orders);

        List<Order> result = orderService.getActiveOrders();

        assertEquals(1, result.size());
        assertNotEquals(OrderStatus.COMPLETED, result.getFirst().getStatus());
        verify(orderRepository, times(1)).findActiveOrders();
    }

    @Test
    @DisplayName("Should find all orders")
    void testFindAllOrders() {
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.findAllOrders();

        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    // ========== Rating Tests ==========

    @Test
    @DisplayName("Should get ratings by order")
    void testGetRatingsByOrder() {
        Rating rating = Rating.builder()
                .id(1L)
                .orderId(1L)
                .jastiperUserId("jastiper-001")
                .ratingValue(5)
                .review("Excellent service")
                .build();

        when(ratingRepository.findByOrderId(1L)).thenReturn(List.of(rating));

        orderService.getRatingsByOrder(1L);

        verify(ratingRepository, times(1)).findByOrderId(1L);
    }

    @Test
    @DisplayName("Should throw exception for invalid status update")
    void testUpdateStatus_InvalidTransition() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order with correct initial status")
    void testCreateOrder() {
        Order newOrder = Order.builder()
                .productName("New Product")
                .quantity(1)
                .shippingAddress("Jakarta")
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        Order result = orderService.createOrder(newOrder);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}

package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.dto.OrderCheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.order.exception.CannotCancelOrderException;
import id.ac.ui.cs.advprog.order.exception.CannotRateOrderException;
import id.ac.ui.cs.advprog.order.exception.InvalidStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.order.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for OrderServiceImpl.
 * Tests checkout, status transitions, cancellation, and rating functionality.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderCheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        // Setup test order
        testOrder = Order.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Limited Sushi")
                .titiperUserId("titiper-001")
                .jastiperUserId("jastiper-001")
                .quantity(2)
                .shippingAddress("Depok, Indonesia")
                .totalPrice(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup checkout request
        checkoutRequest = OrderCheckoutRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .shippingAddress("Depok, Indonesia")
                .titiperUserId("titiper-001")
                .build();
    }

    // ========== Checkout Tests ==========

    @Test
    void testCheckoutOrderSuccess() {
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.checkoutOrder(checkoutRequest);

        assertNotNull(result);
        assertEquals(testOrder.getProductId(), result.getProductId());
        assertEquals(testOrder.getTitiperUserId(), result.getTitiperUserId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    // ========== Order Retrieval Tests ==========

    @Test
    void testGetOrderByIdSuccess() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals(testOrder.getId(), result.get().getId());
    }

    @Test
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetOrdersByTitiper() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByTitiperUserId("titiper-001")).thenReturn(orders);

        List<Order> result = orderService.getOrdersByTitiper("titiper-001");

        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
    }

    @Test
    void testGetOrdersByJastiper() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByJastiperUserId("jastiper-001")).thenReturn(orders);

        List<Order> result = orderService.getOrdersByJastiper("jastiper-001");

        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
    }

    @Test
    void testGetOrdersByStatus() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
    }

    // ========== Status Transition Tests ==========

    @Test
    void testCanTransitionPendingToPaid() {
        assertTrue(orderService.canTransitionStatus(OrderStatus.PENDING, OrderStatus.PAID));
    }

    @Test
    void testCanTransitionPaidToPurchased() {
        assertTrue(orderService.canTransitionStatus(OrderStatus.PAID, OrderStatus.PURCHASED));
    }

    @Test
    void testCanTransitionPurchasedToShipped() {
        assertTrue(orderService.canTransitionStatus(OrderStatus.PURCHASED, OrderStatus.SHIPPED));
    }

    @Test
    void testCanTransitionShippedToCompleted() {
        assertTrue(orderService.canTransitionStatus(OrderStatus.SHIPPED, OrderStatus.COMPLETED));
    }

    @Test
    void testCannotTransitionCompletedToAny() {
        assertFalse(orderService.canTransitionStatus(OrderStatus.COMPLETED, OrderStatus.PAID));
        assertFalse(orderService.canTransitionStatus(OrderStatus.COMPLETED, OrderStatus.PURCHASED));
    }

    @Test
    void testCanTransitionAnyStatusToCancelled() {
        assertTrue(orderService.canTransitionStatus(OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertTrue(orderService.canTransitionStatus(OrderStatus.PAID, OrderStatus.CANCELLED));
        assertTrue(orderService.canTransitionStatus(OrderStatus.PURCHASED, OrderStatus.CANCELLED));
    }

    @Test
    void testUpdateOrderStatusSuccess() {
        Order paidOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

        Order result = orderService.updateOrderStatus(1L, OrderStatus.PURCHASED);

        assertEquals(OrderStatus.PURCHASED, result.getStatus());
    }

    @Test
    void testUpdateOrderStatusNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.updateOrderStatus(999L, OrderStatus.PAID);
        });
    }

    @Test
    void testUpdateOrderStatusInvalidTransition() {
        Order completedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            orderService.updateOrderStatus(1L, OrderStatus.PAID);
        });
    }

    // ========== Cancellation Tests ==========

    @Test
    void testCancelOrderBeforePaid() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Changed my mind")
                .build();

        Order result = orderService.cancelOrder(1L, request);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals("Changed my mind", result.getCancelReason());
        assertNotNull(result.getCancelledAt());
    }

    @Test
    void testCancelOrderAfterShipped() {
        Order shippedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.SHIPPED)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Too late")
                .build();

        assertThrows(CannotCancelOrderException.class, () -> {
            orderService.cancelOrder(1L, request);
        });
    }

    @Test
    void testCancelOrderAfterCompleted() {
        Order completedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Too late")
                .build();

        assertThrows(CannotCancelOrderException.class, () -> {
            orderService.cancelOrder(1L, request);
        });
    }

    @Test
    void testCancelOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Not found")
                .build();

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.cancelOrder(999L, request);
        });
    }

    // ========== Rating Tests ==========

    @Test
    void testSubmitRatingSuccess() {
        Order completedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(1L);
            return rating;
        });

        RatingRequest request = RatingRequest.builder()
                .ratingValue(5)
                .review("Excellent product!")
                .jastiperUserId("jastiper-001")
                .productId("PROD-001")
                .build();

        var result = orderService.submitRating(1L, request);

        assertNotNull(result);
        assertEquals(5, result.getRatingValue());
        assertEquals("Excellent product!", result.getReview());
    }

    @Test
    void testCannotRateIncompleteOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        RatingRequest request = RatingRequest.builder()
                .ratingValue(5)
                .review("Good")
                .jastiperUserId("jastiper-001")
                .productId("PROD-001")
                .build();

        assertThrows(CannotRateOrderException.class, () -> {
            orderService.submitRating(1L, request);
        });
    }

    @Test
    void testGetRatingsByOrder() {
        Rating rating = Rating.builder()
                .id(1L)
                .orderId(1L)
                .ratingValue(5)
                .build();
        when(ratingRepository.findByOrderId(1L)).thenReturn(Arrays.asList(rating));

        var results = orderService.getRatingsByOrder(1L);

        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getRatingValue());
    }

    @Test
    void testGetAverageRatingForJastiper() {
        when(ratingRepository.getAverageRatingForJastiper("jastiper-001"))
                .thenReturn(Optional.of(4.5));

        Optional<Double> result = orderService.getAverageRatingForJastiper("jastiper-001");

        assertTrue(result.isPresent());
        assertEquals(4.5, result.get());
    }

    @Test
    void testGetAverageRatingForProduct() {
        when(ratingRepository.getAverageRatingForProduct("PROD-001"))
                .thenReturn(Optional.of(4.8));

        Optional<Double> result = orderService.getAverageRatingForProduct("PROD-001");

        assertTrue(result.isPresent());
        assertEquals(4.8, result.get());
    }

    // ========== Legacy Method Tests ==========

    @Test
    void testCreateOrderLegacy() {
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(testOrder);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testUpdateStatusLegacy() {
        Order paidOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

        Order result = orderService.updateStatus(1L, "PURCHASED");

        assertEquals(OrderStatus.PURCHASED, result.getStatus());
    }

    @Test
    void testFindAllOrders() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.findAllOrders();

        assertEquals(1, result.size());
    }
}


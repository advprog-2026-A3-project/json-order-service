package id.ac.ui.cs.advprog.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.dto.OrderCheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController REST endpoints.
 * Tests checkout, status updates, cancellation, and rating endpoints.
 */
@WebMvcTest(OrderController.class)
class OrderControllerRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    @MockBean
    private OrderService orderService;

    private Order testOrder;
    private OrderCheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
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

        checkoutRequest = OrderCheckoutRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .shippingAddress("Depok, Indonesia")
                .titiperUserId("titiper-001")
                .build();
    }

    // ========== Checkout Endpoint Tests ==========

    @Test
    void testCheckoutOrderSuccess() throws Exception {
        when(orderService.checkoutOrder(any())).thenReturn(testOrder);

        mockMvc.perform(post("/order/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.productId").value("PROD-001"));
    }

    @Test
    void testCheckoutOrderInvalidRequest() throws Exception {
        OrderCheckoutRequest invalidRequest = OrderCheckoutRequest.builder()
                .productId(null) // Missing required field
                .quantity(2)
                .shippingAddress("Depok")
                .titiperUserId("titiper-001")
                .build();

        mockMvc.perform(post("/order/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== Order Retrieval Endpoint Tests ==========

    @Test
    void testGetOrderByIdSuccess() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/order/api/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productName").value("Limited Sushi"));
    }

    @Test
    void testGetOrderByIdNotFound() throws Exception {
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/order/api/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMyOrders() throws Exception {
        when(orderService.getOrdersByTitiper("titiper-001"))
                .thenReturn(Arrays.asList(testOrder));

        mockMvc.perform(get("/order/api/my-orders/titiper-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].titiperUserId").value("titiper-001"));
    }

    @Test
    void testGetJastipperOrders() throws Exception {
        when(orderService.getOrdersByJastiper("jastiper-001"))
                .thenReturn(Arrays.asList(testOrder));

        mockMvc.perform(get("/order/api/jastiper-orders/jastiper-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jastiperUserId").value("jastiper-001"));
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        when(orderService.getOrdersByStatus(OrderStatus.PENDING))
                .thenReturn(Arrays.asList(testOrder));

        mockMvc.perform(get("/order/api/status/PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void testGetOrdersByStatusInvalid() throws Exception {
        mockMvc.perform(get("/order/api/status/INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetActiveOrders() throws Exception {
        when(orderService.getActiveOrders())
                .thenReturn(Arrays.asList(testOrder));

        mockMvc.perform(get("/order/api/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    // ========== Status Update Endpoint Tests ==========

    @Test
    void testUpdateOrderStatusSuccess() throws Exception {
        Order updatedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();
        when(orderService.updateStatus(1L, "PAID")).thenReturn(updatedOrder);

        mockMvc.perform(put("/order/api/1/status")
                        .param("status", "PAID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void testUpdateOrderStatusInvalidStatus() throws Exception {
        mockMvc.perform(put("/order/api/1/status")
                        .param("status", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== Cancellation Endpoint Tests ==========

    @Test
    void testCancelOrderSuccess() throws Exception {
        Order cancelledOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .cancelReason("Changed my mind")
                .cancelledAt(LocalDateTime.now())
                .build();
        when(orderService.cancelOrder(anyLong(), any())).thenReturn(cancelledOrder);

        CancelOrderRequest request = CancelOrderRequest.builder()
                .reason("Changed my mind")
                .build();

        mockMvc.perform(post("/order/api/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelReason").value("Changed my mind"));
    }

    @Test
    void testCancelOrderInvalidRequest() throws Exception {
        CancelOrderRequest invalidRequest = CancelOrderRequest.builder()
                .reason(null) // Missing required field
                .build();

        mockMvc.perform(post("/order/api/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== Rating Endpoint Tests ==========

    @Test
    void testSubmitRatingSuccess() throws Exception {
        var ratingResponse = new id.ac.ui.cs.advprog.order.dto.RatingResponse(
                1L, 1L, "jastiper-001", "PROD-001", 5, "Excellent!", true, LocalDateTime.now()
        );
        when(orderService.submitRating(anyLong(), any())).thenReturn(ratingResponse);

        RatingRequest request = RatingRequest.builder()
                .ratingValue(5)
                .review("Excellent!")
                .jastiperUserId("jastiper-001")
                .productId("PROD-001")
                .build();

        mockMvc.perform(post("/order/api/1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingValue").value(5))
                .andExpect(jsonPath("$.review").value("Excellent!"));
    }

    @Test
    void testSubmitRatingInvalidValue() throws Exception {
        RatingRequest invalidRequest = RatingRequest.builder()
                .ratingValue(10) // Invalid: should be 1-5
                .review("Too high")
                .jastiperUserId("jastiper-001")
                .productId("PROD-001")
                .build();

        mockMvc.perform(post("/order/api/1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRatingsByOrder() throws Exception {
        var ratings = Arrays.asList(
                new id.ac.ui.cs.advprog.order.dto.RatingResponse(
                        1L, 1L, "jastiper-001", "PROD-001", 5, "Great!", true, LocalDateTime.now()
                )
        );
        when(orderService.getRatingsByOrder(1L)).thenReturn(ratings);

        mockMvc.perform(get("/order/api/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ratingValue").value(5));
    }

    @Test
    void testGetJastipperRatings() throws Exception {
        var ratings = Arrays.asList(
                new id.ac.ui.cs.advprog.order.dto.RatingResponse(
                        1L, 1L, "jastiper-001", "PROD-001", 5, "Great!", true, LocalDateTime.now()
                )
        );
        when(orderService.getRatingsByJastiper("jastiper-001")).thenReturn(ratings);
        when(orderService.getAverageRatingForJastiper("jastiper-001")).thenReturn(Optional.of(5.0));

        mockMvc.perform(get("/order/api/jastiper/jastiper-001/ratings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0));
    }

    @Test
    void testGetProductRatings() throws Exception {
        var ratings = Arrays.asList(
                new id.ac.ui.cs.advprog.order.dto.RatingResponse(
                        1L, 1L, "jastiper-001", "PROD-001", 5, "Great!", true, LocalDateTime.now()
                )
        );
        when(orderService.getRatingsByProduct("PROD-001")).thenReturn(ratings);
        when(orderService.getAverageRatingForProduct("PROD-001")).thenReturn(Optional.of(5.0));

        mockMvc.perform(get("/order/api/product/PROD-001/ratings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0));
    }
}


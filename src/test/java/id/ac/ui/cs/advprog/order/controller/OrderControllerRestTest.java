package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerRestTest {

    private MockMvc mockMvc;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        OrderController controller = new OrderController(orderService);

        Order order = new Order();
        order.setId(1L);
        order.setProductName("Item");
        order.setProductId("PROD-1");
        order.setTitiperUserId("titiper");
        order.setJastiperUserId("jastiper");
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("1000"));
        order.setShippingAddress("Depok");
        order.setStatus(OrderStatus.PENDING);

        when(orderService.getAllOrders()).thenReturn(List.of(order));
        when(orderService.getOrderById(1L)).thenReturn(order);
        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(order);
        when(orderService.updateOrder(any(Long.class), any(OrderCreateRequest.class))).thenReturn(order);
        when(orderService.updateStatus(1L, OrderStatus.PAID)).thenReturn(order);
        when(orderService.cancelOrderById(1L)).thenReturn(order);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getOrderList_returnsOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void postCreate_returnsCreatedOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "productId": "PROD-1",
                              "productName": "Item",
                              "titiperUserId": "titiper",
                              "jastiperUserId": "jastiper",
                              "quantity": 1,
                              "totalPrice": 1000,
                              "shippingAddress": "Depok"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrder_returnsOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void putOrder_returnsUpdatedOrder() throws Exception {
        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "productId": "PROD-1",
                              "productName": "Item",
                              "titiperUserId": "titiper",
                              "jastiperUserId": "jastiper",
                              "quantity": 2,
                              "totalPrice": 2000,
                              "shippingAddress": "Depok"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patchStatus_returnsUpdatedOrder() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void postCancel_returnsUpdatedOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrder_whenMissing_returnsNotFound() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/v1/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void postCreate_whenInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "productName": "Item",
                              "quantity": 1,
                              "totalPrice": 1000,
                              "shippingAddress": "Depok"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productId").exists());
    }
}

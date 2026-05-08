package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private OrderService orderService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        controller = new OrderController(orderService);
    }

    @Test
    void createOrder_returnsCreatedResponse() {
        OrderCreateRequest request = new OrderCreateRequest();
        Order order = sampleOrder(1L);
        when(orderService.createOrder(request)).thenReturn(order);

        ResponseEntity<Order> response = controller.createOrder(request);

        verify(orderService).createOrder(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void getAllOrders_returnsOrders() {
        Order order = sampleOrder(1L);
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        ResponseEntity<List<Order>> response = controller.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getOrderById_returnsOrder() {
        Order order = sampleOrder(2L);
        when(orderService.getOrderById(2L)).thenReturn(order);

        ResponseEntity<Order> response = controller.getOrderById(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void updateOrder_returnsUpdatedOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        Order order = sampleOrder(3L);
        when(orderService.updateOrder(3L, request)).thenReturn(order);

        ResponseEntity<Order> response = controller.updateOrder(3L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void updateOrderStatus_returnsUpdatedOrder() {
        Order order = sampleOrder(4L);
        when(orderService.updateStatus(4L, OrderStatus.PAID)).thenReturn(order);

        ResponseEntity<Order> response = controller.updateOrderStatus(4L, OrderStatus.PAID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void cancelOrder_returnsUpdatedOrder() {
        Order order = sampleOrder(5L);
        when(orderService.cancelOrderById(5L)).thenReturn(order);

        ResponseEntity<Order> response = controller.cancelOrder(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    private Order sampleOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setProductId("PROD-001");
        order.setProductName("Item");
        order.setTitiperUserId("titiper-1");
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("10000"));
        order.setShippingAddress("Depok");
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
}
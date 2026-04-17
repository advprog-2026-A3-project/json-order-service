package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.OrderMapper;
import id.ac.ui.cs.advprog.order.service.OrderService;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private OrderService orderService;
    private OrderMapper orderMapper;
    private RatingService ratingService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        orderMapper = mock(OrderMapper.class);
        ratingService = mock(RatingService.class);
        controller = new OrderController(orderService, orderMapper, ratingService);
    }

    @Test
    void createOrderPage_returnsTemplateAndFormObject() {
        Model model = new ExtendedModelMap();

        String view = controller.createOrderPage(model);

        assertEquals("create-order", view);
        assertNotNull(model.getAttribute("checkoutRequest"));
        assertInstanceOf(OrderCreateRequest.class, model.getAttribute("checkoutRequest"));
    }

    @Test
    void createOrder_redirectsToListWithSuccessMessage() {
        OrderCreateRequest request = new OrderCreateRequest();

        String redirect = controller.createOrder(request);

        verify(orderService).createOrder(request);
        assertEquals("redirect:/order/list?success=Order+berhasil+disimpan", redirect);
    }

    @Test
    void orderList_populatesOrdersRatingsAndViewerFlags() {
        Model model = new ExtendedModelMap();
        Order order = sampleOrder(1L);
        when(orderService.getAllOrders()).thenReturn(List.of(order));
        when(ratingService.getRatingsByOrderIds(List.of(1L))).thenReturn(Collections.emptyMap());

        String view = controller.orderList("ok", null, "titiper", model);

        assertEquals("order-list", view);
        assertEquals("ok", model.getAttribute("successMessage"));
        assertEquals("titiper", model.getAttribute("viewer"));
        assertEquals(true, model.getAttribute("isTitiperView"));
        assertEquals(1, ((List<?>) model.getAttribute("orders")).size());
    }

    @Test
    void editOrderPage_returnsEditTemplateForExistingOrder() {
        Model model = new ExtendedModelMap();
        Order order = sampleOrder(2L);
        OrderCreateRequest request = new OrderCreateRequest();

        when(orderService.getOrderById(2L)).thenReturn(order);
        when(orderMapper.toRequest(order)).thenReturn(request);

        String view = controller.editOrderPage(2L, model);

        assertEquals("edit-order", view);
        assertEquals(2L, model.getAttribute("orderId"));
        assertEquals(request, model.getAttribute("checkoutRequest"));
        assertEquals(OrderStatus.PENDING, model.getAttribute("status"));
    }

    @Test
    void updateOrderStatus_redirectsWithSuccessMessageAndViewer() {
        String redirect = controller.updateOrderStatus(10L, OrderStatus.PAID, "jastiper");

        verify(orderService).updateStatus(10L, OrderStatus.PAID);
        assertEquals("redirect:/order/list?viewer=jastiper&success=Status+order+berhasil+diupdate", redirect);
    }

    @Test
    void cancelOrder_redirectsWithSuccessMessageAndViewer() {
        String redirect = controller.cancelOrder(10L, "jastiper");

        verify(orderService).cancelOrderById(10L);
        assertEquals("redirect:/order/list?viewer=jastiper&success=Order+berhasil+dibatalkan", redirect);
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
package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.OrderMapper;
import id.ac.ui.cs.advprog.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class OrderControllerRestTest {

    private MockMvc mockMvc;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderController controller = new OrderController(orderService, orderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setProductName("Item");
        order.setProductId("PROD-1");
        order.setTitiperUserId("titiper");
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("1000"));
        order.setShippingAddress("Depok");
        order.setStatus(OrderStatus.PENDING);

        when(orderService.getAllOrders()).thenReturn(List.of(order));
        when(orderService.getOrderById(1L)).thenReturn(order);
        when(orderMapper.toRequest(order)).thenReturn(new OrderCreateRequest());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new OrderExceptionHandler())
                .build();
    }

    @Test
    void getOrderList_returnsOrderListView() throws Exception {
        mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-list"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void postCreate_redirectsToList() throws Exception {
        mockMvc.perform(post("/order/create")
                        .param("productId", "PROD-1")
                        .param("productName", "Item")
                        .param("titiperUserId", "titiper")
                        .param("jastiperUserId", "jastiper")
                        .param("quantity", "1")
                        .param("totalPrice", "1000")
                        .param("shippingAddress", "Depok"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?success=Order+berhasil+disimpan"));
    }

    @Test
    void getEdit_returnsEditView() throws Exception {
        mockMvc.perform(get("/order/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-order"))
                .andExpect(model().attributeExists("checkoutRequest"));
    }

    @Test
    void postStatus_redirectsToSuccessMessage() throws Exception {
        mockMvc.perform(post("/order/status/1")
                        .param("status", "PAID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?success=Status+order+berhasil+diupdate"));
    }

    @Test
    void postCancel_redirectsToSuccessMessage() throws Exception {
        mockMvc.perform(post("/order/cancel/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?success=Order+berhasil+dibatalkan"));
    }

    @Test
    void getEdit_whenOrderMissing_redirectsToErrorPageViaAdvice() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/order/edit/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?error=Order+tidak+ditemukan"));
    }

    @Test
    void postCancel_whenTransitionInvalid_redirectsToErrorPageViaAdvice() throws Exception {
        when(orderService.cancelOrderById(1L))
                .thenThrow(new InvalidOrderStatusTransitionException(OrderStatus.SHIPPED, OrderStatus.CANCELLED));

        mockMvc.perform(post("/order/cancel/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?error=Transisi+status+order+tidak+valid"));
    }

    @Test
    void postCreate_whenSelfPurchase_redirectsToErrorPageViaAdvice() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class)))
                .thenThrow(new SelfPurchaseNotAllowedException());

        mockMvc.perform(post("/order/create")
                        .param("productId", "PROD-1")
                        .param("productName", "Item")
                        .param("titiperUserId", "same-user")
                        .param("jastiperUserId", "same-user")
                        .param("quantity", "1")
                        .param("totalPrice", "1000")
                        .param("shippingAddress", "Depok"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?error=Jastiper+tidak+boleh+beli+barang+sendiri"));
    }
}

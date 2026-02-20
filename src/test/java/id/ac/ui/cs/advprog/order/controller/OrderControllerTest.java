package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    void testCreateOrderPage() throws Exception {
        mockMvc.perform(get("/order/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-order"));
    }

    @Test
    void testSubmitOrder() throws Exception {
        mockMvc.perform(post("/order/create")
                        .param("productName", "Limited Sushi")
                        .param("quantity", "2")
                        .param("shippingAddress", "Depok, Indonesia"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list"));
    }

    @Test
    void testListOrders() throws Exception {
        when(orderRepository.findAll()).thenReturn(new ArrayList<Order>());

        mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-list"))
                .andExpect(model().attributeExists("orders"));
    }
}
package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.OrderCreateRequest;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.service.OrderMapper;
import id.ac.ui.cs.advprog.order.service.OrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleOrderControllerTest {

    @Test
    void editOrder_redirectsToErrorWhenIdMissing() {
        OrderService service = mock(OrderService.class);
        OrderMapper mapper = mock(OrderMapper.class);
        OrderController controller = new OrderController(service, mapper);

        OrderCreateRequest request = new OrderCreateRequest();
        when(service.updateOrder(123L, request)).thenThrow(new OrderNotFoundException(123L));

        String redirect = controller.editOrder(123L, request);

        assertEquals("redirect:/order/list?error=Order+tidak+ditemukan", redirect);
    }
}

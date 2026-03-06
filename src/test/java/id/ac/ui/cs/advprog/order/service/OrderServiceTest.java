package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.order.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testCreateOrderLogic() {
        Order order = new Order();
        order.setProductName("Limited Sushi");
        order.setQuantity(2);
        order.setShippingAddress("Depok");

        orderService.createOrder(order);

        verify(orderRepository).save(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void testStatusTransitionValid() {
        assertTrue(orderService.canTransitionStatus(OrderStatus.PENDING, OrderStatus.PAID));
        assertTrue(orderService.canTransitionStatus(OrderStatus.PAID, OrderStatus.PURCHASED));
        assertTrue(orderService.canTransitionStatus(OrderStatus.PURCHASED, OrderStatus.SHIPPED));
        assertTrue(orderService.canTransitionStatus(OrderStatus.SHIPPED, OrderStatus.COMPLETED));
    }

    @Test
    void testStatusTransitionInvalid() {
        assertTrue(!orderService.canTransitionStatus(OrderStatus.COMPLETED, OrderStatus.PAID));
        assertTrue(!orderService.canTransitionStatus(OrderStatus.CANCELLED, OrderStatus.PAID));
        assertTrue(!orderService.canTransitionStatus(OrderStatus.SHIPPED, OrderStatus.PAID));
    }
}
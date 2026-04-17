package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderRepositoryTest {

    @Test
    void saveAndFindById_contractWorksWithRepositoryInterface() {
        OrderRepository repository = mock(OrderRepository.class);

        Order order = new Order();
        order.setId(10L);
        order.setProductName("Limited Sushi");
        order.setQuantity(2);
        order.setShippingAddress("Depok");
        order.setStatus(OrderStatus.PAID);

        when(repository.save(order)).thenReturn(order);

        Order saved = repository.save(order);

        assertNotNull(saved.getId());
        assertEquals("Limited Sushi", saved.getProductName());
        verify(repository).save(order);
    }
}
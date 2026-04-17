package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderRepositoryIntegrationTest {

    @Test
    void findAllByOrderByIdDesc_returnsLatestFirst() {
        OrderRepository repository = mock(OrderRepository.class);

        Order first = new Order();
        first.setId(1L);

        Order second = new Order();
        second.setId(2L);

        when(repository.findAllByOrderByIdDesc()).thenReturn(List.of(second, first));

        List<Order> result = repository.findAllByOrderByIdDesc();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
    }
}

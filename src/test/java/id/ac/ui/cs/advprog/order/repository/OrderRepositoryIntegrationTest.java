package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for OrderRepository.
 * Tests query methods for finding orders by various criteria.
 */
@DataJpaTest
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanUp() {
        orderRepository.deleteAll();
    }

    @Test
    void save_persistsOrderWithGeneratedId() {
        Order order = sampleOrder("PROD-1", "Item A", 1);

        Order saved = orderRepository.save(order);

        assertNotNull(saved.getId());
        assertEquals("PROD-1", saved.getProductId());
    }

    @Test
    void findAllByOrderByIdDesc_returnsLatestFirst() {
        Order first = orderRepository.save(sampleOrder("PROD-1", "Item A", 1));
        Order second = orderRepository.save(sampleOrder("PROD-2", "Item B", 2));

        List<Order> result = orderRepository.findAllByOrderByIdDesc();

        assertFalse(result.isEmpty());
        assertEquals(second.getId(), result.get(0).getId());
        assertEquals(first.getId(), result.get(1).getId());
    }

    private Order sampleOrder(String productId, String productName, int quantity) {
        Order order = new Order();
        order.setProductId(productId);
        order.setProductName(productName);
        order.setTitiperUserId("titiper-1");
        order.setJastiperUserId("jastiper-1");
        order.setQuantity(quantity);
        order.setShippingAddress("Depok");
        order.setTotalPrice(new BigDecimal("10000"));
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
}

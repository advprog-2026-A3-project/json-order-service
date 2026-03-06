package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OrderRepository.
 * Tests query methods for finding orders by various criteria.
 */
@DataJpaTest
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder1;
    private Order testOrder2;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        orderRepository.deleteAll();

        // Create test orders
        testOrder1 = Order.builder()
                .productId("PROD-001")
                .productName("Limited Sushi")
                .titiperUserId("titiper-001")
                .jastiperUserId("jastiper-001")
                .quantity(2)
                .shippingAddress("Depok, Indonesia")
                .totalPrice(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();

        testOrder2 = Order.builder()
                .productId("PROD-002")
                .productName("Premium Ticket")
                .titiperUserId("titiper-002")
                .jastiperUserId("jastiper-001")
                .quantity(1)
                .shippingAddress("Jakarta, Indonesia")
                .totalPrice(new BigDecimal("500.00"))
                .status(OrderStatus.PAID)
                .build();
    }

    @Test
    void testSaveAndFindOrder() {
        Order savedOrder = orderRepository.save(testOrder1);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(orderRepository.findById(savedOrder.getId())).isPresent();
    }

    @Test
    void testFindByTitiperUserId() {
        orderRepository.save(testOrder1);
        orderRepository.save(testOrder2);

        List<Order> result = orderRepository.findByTitiperUserId("titiper-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitiperUserId()).isEqualTo("titiper-001");
    }

    @Test
    void testFindByJastiperUserId() {
        orderRepository.save(testOrder1);
        orderRepository.save(testOrder2);

        List<Order> result = orderRepository.findByJastiperUserId("jastiper-001");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(order -> order.getJastiperUserId().equals("jastiper-001"));
    }

    @Test
    void testFindByStatus() {
        orderRepository.save(testOrder1); // PENDING
        orderRepository.save(testOrder2); // PAID

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> paidOrders = orderRepository.findByStatus(OrderStatus.PAID);

        assertThat(pendingOrders).hasSize(1);
        assertThat(paidOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(paidOrders.get(0).getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void testFindActiveOrders() {
        testOrder1.setStatus(OrderStatus.PENDING);
        testOrder2.setStatus(OrderStatus.COMPLETED);

        orderRepository.save(testOrder1);
        orderRepository.save(testOrder2);

        List<Order> activeOrders = orderRepository.findActiveOrders();

        assertThat(activeOrders).hasSize(1);
        assertThat(activeOrders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testFindByStatusAndJastiperUserId() {
        orderRepository.save(testOrder1); // PENDING, jastiper-001
        orderRepository.save(testOrder2); // PAID, jastiper-001

        List<Order> result = orderRepository.findByStatusAndJastiperUserId(
                OrderStatus.PENDING, "jastiper-001"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testFindByStatusAndTitiperUserId() {
        orderRepository.save(testOrder1); // PENDING, titiper-001
        orderRepository.save(testOrder2); // PAID, titiper-002

        List<Order> result = orderRepository.findByStatusAndTitiperUserId(
                OrderStatus.PENDING, "titiper-001"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitiperUserId()).isEqualTo("titiper-001");
    }

    @Test
    void testOrderVersionFieldForOptimisticLocking() {
        Order savedOrder = orderRepository.save(testOrder1);

        assertThat(savedOrder.getId()).isNotNull();

        // Update the order
        savedOrder.setStatus(OrderStatus.PAID);
        Order updatedOrder = orderRepository.save(savedOrder);

        assertThat(updatedOrder.getId()).isEqualTo(savedOrder.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void testOrderTimestamps() {
        Order savedOrder = orderRepository.save(testOrder1);

        assertThat(savedOrder.getCreatedAt()).isNotNull();
        assertThat(savedOrder.getUpdatedAt()).isNotNull();
        assertThat(savedOrder.getCancelledAt()).isNull(); // Not cancelled
    }
}



package id.ac.ui.cs.advprog.order.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void onCreate_setsTimestampsAndDefaultStatus() {
        Order order = new Order();

        order.onCreate();

        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.getUpdatedAt().isEqual(order.getCreatedAt())
            || order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    void onCreate_keepsExistingStatus() {
        Order order = new Order();
        order.setStatus(OrderStatus.PAID);

        order.onCreate();

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void onUpdate_refreshesUpdatedAt() throws InterruptedException {
        Order order = new Order();
        LocalDateTime initial = LocalDateTime.now().minusMinutes(5);
        order.setUpdatedAt(initial);

        order.onUpdate();

        assertNotNull(order.getUpdatedAt());
        assertTrue(order.getUpdatedAt().isAfter(initial) || order.getUpdatedAt().isEqual(initial));
    }
}


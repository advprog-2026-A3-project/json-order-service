package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders placed by a specific Titiper (buyer)
     */
    List<Order> findByTitiperUserId(String titiperUserId);

    /**
     * Find all orders for a specific Jastiper (seller)
     */
    List<Order> findByJastiperUserId(String jastiperUserId);

    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all active orders (not completed or cancelled)
     */
    @Query("SELECT o FROM Order o WHERE o.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Order> findActiveOrders();

    /**
     * Find orders by both status and Jastiper ID (for Jastiper's to-do list)
     */
    List<Order> findByStatusAndJastiperUserId(OrderStatus status, String jastiperUserId);

    /**
     * Find orders by status and Titiper ID
     */
    List<Order> findByStatusAndTitiperUserId(OrderStatus status, String titiperUserId);
}
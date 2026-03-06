package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.dto.OrderCheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.order.dto.RatingResponse;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for order management.
 * Handles checkout, status transitions, cancellations, and ratings.
 */
public interface OrderService {

    /**
     * Create a new order with checkout validation
     */
    Order checkoutOrder(OrderCheckoutRequest request);

    /**
     * Get order by ID
     */
    Optional<Order> getOrderById(Long id);

    /**
     * Get all orders placed by a Titiper (buyer)
     */
    List<Order> getOrdersByTitiper(String titiperUserId);

    /**
     * Get all orders for a Jastiper (seller)
     */
    List<Order> getOrdersByJastiper(String jastiperUserId);

    /**
     * Get orders by status
     */
    List<Order> getOrdersByStatus(OrderStatus status);

    /**
     * Get active orders (not completed or cancelled)
     */
    List<Order> getActiveOrders();

    /**
     * Get all orders (legacy support)
     */
    List<Order> findAllOrders();

    /**
     * Update order status with transition validation
     */
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * Create order (legacy support)
     */
    Order createOrder(Order order);

    /**
     * Update order status (legacy support)
     */
    Order updateStatus(Long id, String status);

    /**
     * Cancel an order and trigger refund
     */
    Order cancelOrder(Long orderId, CancelOrderRequest request);

    /**
     * Validate if a status transition is allowed
     */
    boolean canTransitionStatus(OrderStatus fromStatus, OrderStatus toStatus);

    /**
     * Submit a rating for an order
     */
    RatingResponse submitRating(Long orderId, RatingRequest request);

    /**
     * Get all ratings for an order
     */
    List<RatingResponse> getRatingsByOrder(Long orderId);

    /**
     * Get all ratings received by a Jastiper
     */
    List<RatingResponse> getRatingsByJastiper(String jastiperUserId);

    /**
     * Get average rating for a Jastiper
     */
    Optional<Double> getAverageRatingForJastiper(String jastiperUserId);

    /**
     * Get average rating for a product
     */
    Optional<Double> getAverageRatingForProduct(String productId);

    /**
     * Get all ratings for a product
     */
    List<RatingResponse> getRatingsByProduct(String productId);
}
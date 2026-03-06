package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.CancelOrderRequest;
import id.ac.ui.cs.advprog.order.dto.OrderCheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.order.dto.RatingResponse;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for order management.
 * Provides endpoints for checkout, status updates, cancellation, and ratings.
 */
@Slf4j
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ========== Legacy Web UI Endpoints (for backward compatibility) ==========

    @GetMapping("/create")
    public String createOrderForm(Model model) {
        model.addAttribute("order", new Order());
        return "create-order";
    }

    @PostMapping("/create")
    public String submitOrder(@ModelAttribute Order order) {
        orderService.createOrder(order);
        return "redirect:/order/list";
    }

    @GetMapping("/list")
    public String orderHistory(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "order-list";
    }

    // ========== REST API Endpoints for Core Functionality ==========

    /**
     * POST /api/order/checkout - Create a new order through checkout
     */
    @PostMapping("/api/checkout")
    public ResponseEntity<?> checkoutOrder(@Valid @RequestBody OrderCheckoutRequest request) {
        log.info("Checkout request received for product: {}", request.getProductId());
        try {
            Order order = orderService.checkoutOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(order));
        } catch (Exception e) {
            log.error("Checkout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Checkout failed: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/order/{id} - Get order details by ID
     */
    @GetMapping("/api/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            return ResponseEntity.ok(convertToResponse(order.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Order not found: " + id)
            );
        }
    }

    /**
     * GET /api/order/my-orders/{titiperUserId} - Get all orders of a Titiper
     */
    @GetMapping("/api/my-orders/{titiperUserId}")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@PathVariable String titiperUserId) {
        List<OrderResponse> orders = orderService.getOrdersByTitiper(titiperUserId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/order/jastiper-orders/{jastiperUserId} - Get all orders for a Jastiper
     */
    @GetMapping("/api/jastiper-orders/{jastiperUserId}")
    public ResponseEntity<List<OrderResponse>> getJastipperOrders(@PathVariable String jastiperUserId) {
        List<OrderResponse> orders = orderService.getOrdersByJastiper(jastiperUserId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/order/status/{status} - Get orders by status
     */
    @GetMapping("/api/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderResponse> orders = orderService.getOrdersByStatus(orderStatus)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Invalid status: " + status)
            );
        }
    }

    /**
     * GET /api/order/active - Get all active orders
     */
    @GetMapping("/api/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders() {
        List<OrderResponse> orders = orderService.getActiveOrders()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    /**
     * PUT /api/order/{id}/status - Update order status
     */
    @PutMapping("/api/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                                @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateStatus(id, status);
            return ResponseEntity.ok(convertToResponse(updatedOrder));
        } catch (Exception e) {
            log.error("Status update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Status update failed: " + e.getMessage())
            );
        }
    }

    /**
     * POST /api/order/{id}/cancel - Cancel an order
     */
    @PostMapping("/api/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id,
                                         @Valid @RequestBody CancelOrderRequest request) {
        log.info("Cancel request for order: {}", id);
        try {
            Order cancelledOrder = orderService.cancelOrder(id, request);
            return ResponseEntity.ok(convertToResponse(cancelledOrder));
        } catch (Exception e) {
            log.error("Cancellation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Cancellation failed: " + e.getMessage())
            );
        }
    }

    // ========== Rating Endpoints ==========

    /**
     * POST /api/order/{id}/rate - Submit a rating for a completed order
     */
    @PostMapping("/api/{id}/rate")
    public ResponseEntity<?> submitRating(@PathVariable Long id,
                                          @Valid @RequestBody RatingRequest request) {
        log.info("Rating submission for order: {}", id);
        try {
            RatingResponse rating = orderService.submitRating(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(rating);
        } catch (Exception e) {
            log.error("Rating submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Rating submission failed: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/order/{id}/ratings - Get all ratings for an order
     */
    @GetMapping("/api/{id}/ratings")
    public ResponseEntity<List<RatingResponse>> getRatingsByOrder(@PathVariable Long id) {
        List<RatingResponse> ratings = orderService.getRatingsByOrder(id);
        return ResponseEntity.ok(ratings);
    }

    /**
     * GET /api/jastiper/{jastiperUserId}/ratings - Get ratings received by a Jastiper
     */
    @GetMapping("/api/jastiper/{jastiperUserId}/ratings")
    public ResponseEntity<?> getJastipperRatings(@PathVariable String jastiperUserId) {
        List<RatingResponse> ratings = orderService.getRatingsByJastiper(jastiperUserId);
        Optional<Double> averageRating = orderService.getAverageRatingForJastiper(jastiperUserId);

        return ResponseEntity.ok(new RatingsAggregate(ratings, averageRating.orElse(0.0)));
    }

    /**
     * GET /api/product/{productId}/ratings - Get ratings for a product
     */
    @GetMapping("/api/product/{productId}/ratings")
    public ResponseEntity<?> getProductRatings(@PathVariable String productId) {
        List<RatingResponse> ratings = orderService.getRatingsByProduct(productId);
        Optional<Double> averageRating = orderService.getAverageRatingForProduct(productId);

        return ResponseEntity.ok(new RatingsAggregate(ratings, averageRating.orElse(0.0)));
    }

    // ========== Helper Methods ==========

    /**
     * Convert Order entity to OrderResponse DTO
     */
    private OrderResponse convertToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .titiperUserId(order.getTitiperUserId())
                .jastiperUserId(order.getJastiperUserId())
                .quantity(order.getQuantity())
                .shippingAddress(order.getShippingAddress())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .build();
    }

    /**
     * Simple error response DTO
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }

    /**
     * Ratings aggregate DTO for combined ratings info
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class RatingsAggregate {
        private List<RatingResponse> ratings;
        private Double averageRating;
    }
}
package id.ac.ui.cs.advprog.order.exception;

/**
 * Exception thrown when order is not found.
 */
public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(Long orderId) {
        super(String.format("Order with ID %d not found", orderId));
    }
}


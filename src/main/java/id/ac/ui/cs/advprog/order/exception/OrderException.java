package id.ac.ui.cs.advprog.order.exception;

/**
 * Base exception class for order-related errors.
 */
public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}


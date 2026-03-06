package id.ac.ui.cs.advprog.order.exception;

/**
 * Exception thrown when trying to rate an order that is not yet completed.
 */
public class CannotRateOrderException extends OrderException {
    public CannotRateOrderException(Long orderId, String currentStatus) {
        super(String.format("Cannot rate order %d with status %s. Orders can only be rated after completion.", orderId, currentStatus));
    }
}


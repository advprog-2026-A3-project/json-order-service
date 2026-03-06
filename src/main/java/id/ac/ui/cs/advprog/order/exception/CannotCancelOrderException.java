package id.ac.ui.cs.advprog.order.exception;

/**
 * Exception thrown when trying to cancel an order after it has shipped.
 */
public class CannotCancelOrderException extends OrderException {
    public CannotCancelOrderException(Long orderId, String currentStatus) {
        super(String.format("Cannot cancel order %d with status %s. Orders can only be cancelled before shipping.", orderId, currentStatus));
    }
}


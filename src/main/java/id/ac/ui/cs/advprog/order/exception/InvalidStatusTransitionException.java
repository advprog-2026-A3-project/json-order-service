package id.ac.ui.cs.advprog.order.exception;

/**
 * Exception thrown when an invalid status transition is attempted.
 */
public class InvalidStatusTransitionException extends OrderException {
    public InvalidStatusTransitionException(String fromStatus, String toStatus) {
        super(String.format("Cannot transition from status %s to %s", fromStatus, toStatus));
    }
}


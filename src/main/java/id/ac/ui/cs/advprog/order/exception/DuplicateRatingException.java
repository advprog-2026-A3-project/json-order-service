package id.ac.ui.cs.advprog.order.exception;

public class DuplicateRatingException extends OrderException {
    public DuplicateRatingException(Long orderId) {
        super("Rating already exists for order ID: " + orderId);
    }
}


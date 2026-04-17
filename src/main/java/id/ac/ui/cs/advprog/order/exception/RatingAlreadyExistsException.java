package id.ac.ui.cs.advprog.order.exception;

public class RatingAlreadyExistsException extends RuntimeException {
    public RatingAlreadyExistsException(Long orderId) {
        super("Order " + orderId + " sudah memiliki rating");
    }
}


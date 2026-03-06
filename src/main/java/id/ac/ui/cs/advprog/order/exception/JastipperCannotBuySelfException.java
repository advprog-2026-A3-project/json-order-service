package id.ac.ui.cs.advprog.order.exception;

/**
 * Exception thrown when a Jastiper tries to buy their own product.
 */
public class JastipperCannotBuySelfException extends OrderException {
    public JastipperCannotBuySelfException(String userId) {
        super(String.format("Jastiper %s cannot purchase their own products", userId));
    }
}


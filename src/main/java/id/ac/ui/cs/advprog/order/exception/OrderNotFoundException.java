package id.ac.ui.cs.advprog.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order dengan id " + id + " tidak ditemukan");
    }
}


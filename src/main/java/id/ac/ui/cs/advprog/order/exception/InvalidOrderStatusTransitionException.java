package id.ac.ui.cs.advprog.order.exception;

import id.ac.ui.cs.advprog.order.model.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Transisi status order tidak valid dari " + currentStatus + " ke " + targetStatus);
    }
}


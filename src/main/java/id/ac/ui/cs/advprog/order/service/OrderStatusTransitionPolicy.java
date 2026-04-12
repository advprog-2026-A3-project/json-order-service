package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusTransitionPolicy {

    public boolean canTransition(OrderStatus fromStatus, OrderStatus toStatus) {
        if (fromStatus == toStatus) {
            return true;
        }

        return switch (fromStatus) {
            case PENDING -> toStatus == OrderStatus.PAID || toStatus == OrderStatus.CANCELLED;
            case PAID -> toStatus == OrderStatus.PURCHASED || toStatus == OrderStatus.CANCELLED;
            case PURCHASED -> toStatus == OrderStatus.SHIPPED || toStatus == OrderStatus.CANCELLED;
            case SHIPPED -> toStatus == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}


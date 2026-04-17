package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.RatingAlreadyExistsException;
import id.ac.ui.cs.advprog.order.exception.RatingNotAllowedException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleOrderControllerTest {

    private final OrderExceptionHandler handler = new OrderExceptionHandler();

    @Test
    void handleOrderNotFound_returnsConsistentRedirect() {
        String redirect = handler.handleOrderNotFound(new OrderNotFoundException(123L));

        assertEquals("redirect:/order/list?error=Order+tidak+ditemukan", redirect);
    }

    @Test
    void handleInvalidTransition_returnsConsistentRedirect() {
        String redirect = handler.handleInvalidTransition(
                new InvalidOrderStatusTransitionException(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
        );

        assertEquals("redirect:/order/list?error=Transisi+status+order+tidak+valid", redirect);
    }

    @Test
    void handleSelfPurchase_returnsConsistentRedirect() {
        String redirect = handler.handleSelfPurchase(new SelfPurchaseNotAllowedException());

        assertEquals("redirect:/order/list?error=Jastiper+tidak+boleh+beli+barang+sendiri", redirect);
    }

    @Test
    void handleRatingAlreadyExists_returnsConsistentRedirect() {
        String redirect = handler.handleRatingAlreadyExists(new RatingAlreadyExistsException(1L));

        assertEquals("redirect:/order/list?error=Order+ini+sudah+pernah+dirating", redirect);
    }

    @Test
    void handleRatingNotAllowed_returnsConsistentRedirect() {
        String redirect = handler.handleRatingNotAllowed(new RatingNotAllowedException("Rating hanya untuk completed"));

        assertEquals("redirect:/order/list?error=Rating+hanya+untuk+completed", redirect);
    }
}

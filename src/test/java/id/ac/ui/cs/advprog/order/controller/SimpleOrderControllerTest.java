package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.RatingAlreadyExistsException;
import id.ac.ui.cs.advprog.order.exception.RatingNotAllowedException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleOrderControllerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleOrderNotFound_returnsNotFoundResponse() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleOrderNotFound(new OrderNotFoundException(123L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Order dengan id 123 tidak ditemukan", response.getBody().getMessage());
    }

    @Test
    void handleInvalidTransition_returnsBadRequestResponse() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleInvalidOrderStatusTransition(
                        new InvalidOrderStatusTransitionException(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Transisi status order tidak valid dari SHIPPED ke CANCELLED", response.getBody().getMessage());
    }

    @Test
    void handleSelfPurchase_returnsForbiddenResponse() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleSelfPurchaseNotAllowed(new SelfPurchaseNotAllowedException());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Jastiper tidak boleh membeli barang miliknya sendiri", response.getBody().getMessage());
    }

    @Test
    void handleRatingAlreadyExists_returnsConflictResponse() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleRatingAlreadyExists(new RatingAlreadyExistsException(1L));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Order 1 sudah memiliki rating", response.getBody().getMessage());
    }

    @Test
    void handleRatingNotAllowed_returnsBadRequestResponse() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleRatingNotAllowed(new RatingNotAllowedException("Rating hanya untuk completed"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Rating hanya untuk completed", response.getBody().getMessage());
    }
}

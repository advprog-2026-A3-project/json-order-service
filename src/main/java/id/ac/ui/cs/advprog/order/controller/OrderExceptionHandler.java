package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public String handleOrderNotFound(OrderNotFoundException ex) {
        return "redirect:/order/list?error=Order+tidak+ditemukan";
    }

    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public String handleInvalidTransition(InvalidOrderStatusTransitionException ex) {
        return "redirect:/order/list?error=Transisi+status+order+tidak+valid";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationError(MethodArgumentNotValidException ex) {
        return "redirect:/order/list?error=Data+order+tidak+valid";
    }
}


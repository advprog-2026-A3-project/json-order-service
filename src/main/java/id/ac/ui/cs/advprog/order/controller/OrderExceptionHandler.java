package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.exception.InvalidOrderStatusTransitionException;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(SelfPurchaseNotAllowedException.class)
    public String handleSelfPurchase(SelfPurchaseNotAllowedException ex) {
        return "redirect:/order/list?error=Jastiper+tidak+boleh+beli+barang+sendiri";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationError(MethodArgumentNotValidException ex) {
        return "redirect:/order/list?error=Data+order+tidak+valid";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return "redirect:/order/list?error=Nilai+request+tidak+valid";
    }
}

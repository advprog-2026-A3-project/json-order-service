package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.order.dto.CheckoutResponse;
import id.ac.ui.cs.advprog.order.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderRestController {
    private final CheckoutService checkoutService;

    public OrderRestController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(checkoutService.checkout(request));
    }
}
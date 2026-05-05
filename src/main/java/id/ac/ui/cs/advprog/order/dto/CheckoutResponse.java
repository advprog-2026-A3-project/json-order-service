package id.ac.ui.cs.advprog.order.dto;

import id.ac.ui.cs.advprog.order.model.OrderStatus;
import java.math.BigDecimal;

public record CheckoutResponse(
    Long orderId,
    OrderStatus status,
    String voucherCode,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal totalPaid
){}
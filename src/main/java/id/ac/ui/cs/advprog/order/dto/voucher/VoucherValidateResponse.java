package id.ac.ui.cs.advprog.order.dto.voucher;

import java.math.BigDecimal;

public record VoucherValidateResponse(
    String voucherCode,
    BigDecimal subtotal,
    BigDecimal discountAmount
){}
package id.ac.ui.cs.advprog.order.dto.voucher;

import java.math.BigDecimal;

public record VoucherRedeemResponse(
    String voucherCode,
    BigDecimal subtotal,
    Integer quotaRemaining
){}
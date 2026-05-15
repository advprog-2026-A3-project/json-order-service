package id.ac.ui.cs.advprog.order.dto.voucher;

public record VoucherValidateRequest(
    String voucherCode,
    Long subtotal
){}

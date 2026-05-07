package id.ac.ui.cs.advprog.order.client;

import id.ac.ui.cs.advprog.order.dto.voucher.VoucherRedeemRequest;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherRedeemResponse;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherValidateRequest;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherValidateResponse;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VoucherClient {
    private final RestClient restClient;

    public VoucherClient(
        RestClient.Builder restClientBuilder,
        @Value("${voucher.service.base-url}") String voucherServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
            .baseUrl(voucherServiceBaseUrl)
            .build();
    }

    public VoucherValidateResponse validateVoucher(String voucherCode, BigDecimal subtotal) {
        return restClient.post()
            .uri("/api/v1/vouchers/validate")
            .body(new VoucherValidateRequest(voucherCode, toVoucherSubtotal(subtotal)))
            .retrieve()
            .body(VoucherValidateResponse.class);
    }

    public VoucherRedeemResponse redeemVoucher(String voucherCode, BigDecimal subtotal) {
        return restClient.post()
            .uri("/api/v1/vouchers/{voucherCode}/redeem", voucherCode)
            .body(new VoucherRedeemRequest(toVoucherSubtotal(subtotal)))
            .retrieve()
            .body(VoucherRedeemResponse.class);
    }

    private Long toVoucherSubtotal(BigDecimal subtotal) {
        if (subtotal == null) {
            throw new IllegalArgumentException("subtotal is required");
        }

        try {
            return subtotal.longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("subtotal must be a whole number", exception);
        }
    }
}

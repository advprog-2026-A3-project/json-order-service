package id.ac.ui.cs.advprog.order.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import id.ac.ui.cs.advprog.order.dto.voucher.VoucherRedeemResponse;
import id.ac.ui.cs.advprog.order.dto.voucher.VoucherValidateResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class VoucherClientTest {
    private MockRestServiceServer server;
    private VoucherClient voucherClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        voucherClient = new VoucherClient(restClientBuilder, "http://voucher-service");
    }

    @Test
    void validateVoucher_postsValidateRequestAndReturnsResponse() {
        server.expect(requestTo("http://voucher-service/api/v1/vouchers/validate"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""
                {
                  "voucherCode": "WELCOME20",
                  "subtotal": 200000
                }
                """))
            .andRespond(withSuccess("""
                {
                  "voucherCode": "WELCOME20",
                  "subtotal": 200000,
                  "discountAmount": 40000
                }
                """, MediaType.APPLICATION_JSON));

        VoucherValidateResponse response = voucherClient.validateVoucher("WELCOME20", new BigDecimal("200000"));

        assertEquals("WELCOME20", response.voucherCode());
        assertEquals(0, new BigDecimal("200000").compareTo(response.subtotal()));
        assertEquals(0, new BigDecimal("40000").compareTo(response.discountAmount()));
        server.verify();
    }

    @Test
    void redeemVoucher_postsRedeemRequestAndReturnsResponse() {
        server.expect(requestTo("http://voucher-service/api/v1/vouchers/WELCOME20/redeem"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""
                {
                  "subtotal": 200000
                }
                """))
            .andRespond(withSuccess("""
                {
                  "voucherCode": "WELCOME20",
                  "subtotal": 200000,
                  "quotaRemaining": 9
                }
                """, MediaType.APPLICATION_JSON));

        VoucherRedeemResponse response = voucherClient.redeemVoucher("WELCOME20", new BigDecimal("200000"));

        assertEquals("WELCOME20", response.voucherCode());
        assertEquals(0, new BigDecimal("200000").compareTo(response.subtotal()));
        assertEquals(9, response.quotaRemaining());
        server.verify();
    }

    @Test
    void validateVoucher_throwsWhenVoucherServiceRejectsRequest() {
        server.expect(requestTo("http://voucher-service/api/v1/vouchers/validate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withBadRequest());

        assertThrows(
            RestClientResponseException.class,
            () -> voucherClient.validateVoucher("INVALID", new BigDecimal("200000"))
        );
        server.verify();
    }

    @Test
    void validateVoucher_rejectsDecimalSubtotalBeforeCallingVoucherService() {
        assertThrows(
            IllegalArgumentException.class,
            () -> voucherClient.validateVoucher("WELCOME20", new BigDecimal("200000.50"))
        );
    }

    @Test
    void redeemVoucher_rejectsNullSubtotalBeforeCallingVoucherService() {
        assertThrows(
            IllegalArgumentException.class,
            () -> voucherClient.redeemVoucher("WELCOME20", null)
        );
    }
}

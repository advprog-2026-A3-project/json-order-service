package id.ac.ui.cs.advprog.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.order.dto.CheckoutResponse;
import id.ac.ui.cs.advprog.order.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.order.exception.SelfPurchaseNotAllowedException;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.service.CheckoutService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderRestControllerTest {
    private MockMvc mockMvc;
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = mock(CheckoutService.class);
        OrderRestController controller = new OrderRestController(checkoutService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void checkout_returnsCheckoutResponse() throws Exception {
        when(checkoutService.checkout(any()))
            .thenReturn(new CheckoutResponse(
                1L,
                OrderStatus.PAID,
                "WELCOME20",
                new BigDecimal("200000"),
                new BigDecimal("40000"),
                new BigDecimal("160000")
            ));

        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": "PROD-001",
                      "productName": "KitKat Matcha",
                      "titiperUserId": "titiper-1",
                      "jastiperUserId": "jastiper-1",
                      "quantity": 2,
                      "subtotal": 200000,
                      "shippingAddress": "Depok",
                      "voucherCode": "WELCOME20"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(1))
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.voucherCode").value("WELCOME20"))
            .andExpect(jsonPath("$.subtotal").value(200000))
            .andExpect(jsonPath("$.discountAmount").value(40000))
            .andExpect(jsonPath("$.totalPaid").value(160000));
    }

    @Test
    void checkout_whenRequestInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productName": "KitKat Matcha",
                      "titiperUserId": "titiper-1",
                      "jastiperUserId": "jastiper-1",
                      "quantity": 2,
                      "subtotal": 200000,
                      "shippingAddress": "Depok"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.productId").exists());
    }

    @Test
    void checkout_whenSubtotalHasDecimal_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": "PROD-001",
                      "productName": "KitKat Matcha",
                      "titiperUserId": "titiper-1",
                      "jastiperUserId": "jastiper-1",
                      "quantity": 2,
                      "subtotal": 200000.50,
                      "shippingAddress": "Depok",
                      "voucherCode": "WELCOME20"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.subtotal").exists());
    }

    @Test
    void checkout_whenSelfPurchase_returnsForbidden() throws Exception {
        when(checkoutService.checkout(any()))
            .thenThrow(new SelfPurchaseNotAllowedException());

        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": "PROD-001",
                      "productName": "KitKat Matcha",
                      "titiperUserId": "same-user",
                      "jastiperUserId": "same-user",
                      "quantity": 2,
                      "subtotal": 200000,
                      "shippingAddress": "Depok",
                      "voucherCode": "WELCOME20"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Jastiper tidak boleh membeli barang miliknya sendiri"));
    }

    @Test
    void checkout_whenVoucherServiceRejectsRequest_returnsDownstreamError() throws Exception {
        when(checkoutService.checkout(any()))
            .thenThrow(HttpClientErrorException.BadRequest.create(
                "Bad Request",
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Bad Request",
                org.springframework.http.HttpHeaders.EMPTY,
                """
                {"status":"ERROR","message":"voucher is inactive"}
                """.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": "PROD-001",
                      "productName": "KitKat Matcha",
                      "titiperUserId": "titiper-1",
                      "jastiperUserId": "jastiper-1",
                      "quantity": 2,
                      "subtotal": 200000,
                      "shippingAddress": "Depok",
                      "voucherCode": "WELCOME20"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("voucher is inactive"));
    }

    @Test
    void checkout_whenVoucherQuotaExhausted_returnsConflict() throws Exception {
        when(checkoutService.checkout(any()))
            .thenThrow(HttpClientErrorException.create(
                org.springframework.http.HttpStatus.CONFLICT,
                "Conflict",
                org.springframework.http.HttpHeaders.EMPTY,
                """
                {"status":"ERROR","message":"voucher quota exhausted"}
                """.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": "PROD-001",
                      "productName": "KitKat Matcha",
                      "titiperUserId": "titiper-1",
                      "jastiperUserId": "jastiper-1",
                      "quantity": 2,
                      "subtotal": 200000,
                      "shippingAddress": "Depok",
                      "voucherCode": "WELCOME20"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("voucher quota exhausted"));
    }

    @Test
    void checkout_whenVoucherServiceUnavailable_returnsServiceUnavailable() throws Exception {
        when(checkoutService.checkout(any()))
            .thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": "PROD-001",
                      "productName": "KitKat Matcha",
                      "titiperUserId": "titiper-1",
                      "jastiperUserId": "jastiper-1",
                      "quantity": 2,
                      "subtotal": 200000,
                      "shippingAddress": "Depok",
                      "voucherCode": "WELCOME20"
                    }
                    """))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.message").value("Downstream service unavailable"));
    }
}
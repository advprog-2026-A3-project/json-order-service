package id.ac.ui.cs.advprog.order.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleRestClientResponseException_extractsMessageFromJsonBody() {
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.CONFLICT,
            "Conflict",
            HttpHeaders.EMPTY,
            """
            {"status":"ERROR","message":"voucher quota exhausted"}
            """.getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8
        );

        GlobalExceptionHandler.ErrorResponse response = handler
            .handleRestClientResponseException(exception)
            .getBody();

        assertEquals(409, response.getStatus());
        assertEquals("voucher quota exhausted", response.getMessage());
    }

    @Test
    void handleRestClientResponseException_fallsBackToRawBodyWhenJsonIsInvalid() {
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            "plain downstream error".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8
        );

        GlobalExceptionHandler.ErrorResponse response = handler
            .handleRestClientResponseException(exception)
            .getBody();

        assertEquals(400, response.getStatus());
        assertEquals("plain downstream error", response.getMessage());
    }

    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleIllegalArgumentException(new IllegalArgumentException("subtotal must be a whole number"))
            .getBody();

        assertEquals(400, response.getStatus());
        assertEquals("subtotal must be a whole number", response.getMessage());
    }

    @Test
    void handleGenericException_returnsForbiddenWhenSelfPurchaseIsWrapped() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleGenericException(new RuntimeException(new SelfPurchaseNotAllowedException()))
            .getBody();

        assertEquals(403, response.getStatus());
        assertEquals("Jastiper tidak boleh membeli barang miliknya sendiri", response.getMessage());
    }

    @Test
    void handleOrderNotFound_returnsNotFound() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleOrderNotFound(new OrderNotFoundException(42L))
            .getBody();

        assertEquals(404, response.getStatus());
        assertEquals("Order dengan id 42 tidak ditemukan", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleInvalidStatusTransition_returnsBadRequest() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleInvalidStatusTransition(new InvalidStatusTransitionException("PAID", "COMPLETED"))
            .getBody();

        assertEquals(400, response.getStatus());
        assertEquals("Cannot transition from status PAID to COMPLETED", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleCannotCancelOrder_returnsBadRequest() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleCannotCancelOrder(new CannotCancelOrderException(7L, "SHIPPED"))
            .getBody();

        assertEquals(400, response.getStatus());
        assertEquals(
            "Cannot cancel order 7 with status SHIPPED. Orders can only be cancelled before shipping.",
            response.getMessage()
        );
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleCannotRateOrder_returnsBadRequest() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleCannotRateOrder(new CannotRateOrderException(8L, "PAID"))
            .getBody();

        assertEquals(400, response.getStatus());
        assertEquals(
            "Cannot rate order 8 with status PAID. Orders can only be rated after completion.",
            response.getMessage()
        );
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleJastipperCannotBuySelf_returnsForbidden() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleJastipperCannotBuySelf(new JastipperCannotBuySelfException("user-123"))
            .getBody();

        assertEquals(403, response.getStatus());
        assertEquals("Jastiper user-123 cannot purchase their own products", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleSelfPurchaseNotAllowed_returnsForbidden() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleSelfPurchaseNotAllowed(new SelfPurchaseNotAllowedException())
            .getBody();

        assertEquals(403, response.getStatus());
        assertEquals("Jastiper tidak boleh membeli barang miliknya sendiri", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleOrderException_returnsBadRequest() {
        GlobalExceptionHandler.ErrorResponse response = handler
            .handleOrderException(new OrderException("order error"))
            .getBody();

        assertEquals(400, response.getStatus());
        assertEquals("order error", response.getMessage());
        assertNotNull(response.getTimestamp());
    }
}

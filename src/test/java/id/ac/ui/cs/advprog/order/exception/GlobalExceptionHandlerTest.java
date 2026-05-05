package id.ac.ui.cs.advprog.order.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}

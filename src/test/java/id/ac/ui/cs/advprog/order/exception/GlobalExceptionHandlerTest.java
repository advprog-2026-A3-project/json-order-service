package id.ac.ui.cs.advprog.order.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for GlobalExceptionHandler
 * Tests all exception handling and error responses
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }

        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        
        @GetMapping("/test/order-not-found")
        public void throwOrderNotFound() {
            throw new OrderNotFoundException(999L);
        }

        @GetMapping("/test/invalid-status-transition")
        public void throwInvalidStatusTransition() {
            throw new InvalidStatusTransitionException("PENDING", "COMPLETED");
        }

        @GetMapping("/test/cannot-cancel-order")
        public void throwCannotCancelOrder() {
            throw new CannotCancelOrderException(1L, "Order already shipped");
        }

        @GetMapping("/test/cannot-rate-order")
        public void throwCannotRateOrder() {
            throw new CannotRateOrderException(1L, "Order not completed");
        }

        @GetMapping("/test/jastiper-cannot-buy-self")
        public void throwJastipperCannotBuySelf() {
            throw new JastipperCannotBuySelfException("jastiper-001");
        }

        @GetMapping("/test/order-exception")
        public void throwOrderException() {
            throw new OrderException("Generic order error");
        }

        @GetMapping("/test/generic-exception")
        public void throwGenericException() {
            throw new RuntimeException("Unexpected error");
        }

        @PostMapping("/test/validation")
        public void testValidation(@Valid @RequestBody TestRequest request) {
            // Should not reach here if validation fails
        }
    }

    @Data
    static class TestRequest {
        @NotBlank(message = "Product name cannot be blank")
        private String productName;

        @NotBlank(message = "Address cannot be blank")
        private String address;
    }

    // ========== OrderNotFoundException Tests ==========

    @Test
    @DisplayName("Should handle OrderNotFoundException with 404 status")
    void testOrderNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("OrderNotFoundException should include order ID in message")
    void testOrderNotFound_IncludesOrderId() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }

    // ========== InvalidStatusTransitionException Tests ==========

    @Test
    @DisplayName("Should handle InvalidStatusTransitionException with 400 status")
    void testInvalidStatusTransition_Returns400() throws Exception {
        mockMvc.perform(get("/test/invalid-status-transition"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("InvalidStatusTransitionException should include status information")
    void testInvalidStatusTransition_IncludesStatuses() throws Exception {
        mockMvc.perform(get("/test/invalid-status-transition"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("PENDING") | containsString("COMPLETED")));
    }

    // ========== CannotCancelOrderException Tests ==========

    @Test
    @DisplayName("Should handle CannotCancelOrderException with 400 status")
    void testCannotCancelOrder_Returns400() throws Exception {
        mockMvc.perform(get("/test/cannot-cancel-order"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("CannotCancelOrderException should include order ID and reason")
    void testCannotCancelOrder_IncludesDetails() throws Exception {
        mockMvc.perform(get("/test/cannot-cancel-order"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("1")));
    }

    // ========== CannotRateOrderException Tests ==========

    @Test
    @DisplayName("Should handle CannotRateOrderException with 400 status")
    void testCannotRateOrder_Returns400() throws Exception {
        mockMvc.perform(get("/test/cannot-rate-order"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("CannotRateOrderException should include order ID")
    void testCannotRateOrder_IncludesOrderId() throws Exception {
        mockMvc.perform(get("/test/cannot-rate-order"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("1")));
    }

    // ========== JastipperCannotBuySelfException Tests ==========

    @Test
    @DisplayName("Should handle JastipperCannotBuySelfException with 403 status")
    void testJastipperCannotBuySelf_Returns403() throws Exception {
        mockMvc.perform(get("/test/jastiper-cannot-buy-self"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("JastipperCannotBuySelfException should include user ID")
    void testJastipperCannotBuySelf_IncludesUserId() throws Exception {
        mockMvc.perform(get("/test/jastiper-cannot-buy-self"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("jastiper-001")));
    }

    // ========== OrderException Tests ==========

    @Test
    @DisplayName("Should handle OrderException with 400 status")
    void testOrderException_Returns400() throws Exception {
        mockMvc.perform(get("/test/order-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Generic order error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== Generic Exception Tests ==========

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void testGenericException_Returns500() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== Validation Exception Tests ==========

    @Test
    @DisplayName("Should handle validation errors with 400 status")
    void testValidationError_Returns400() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Validation error should include field errors")
    void testValidationError_IncludesFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productName").exists())
                .andExpect(jsonPath("$.errors.address").exists());
    }

    @Test
    @DisplayName("Validation error should include error messages")
    void testValidationError_IncludesMessages() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productName").value(containsString("blank")))
                .andExpect(jsonPath("$.errors.address").value(containsString("blank")));
    }

    @Test
    @DisplayName("Validation should pass with valid request")
    void testValidation_Success() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productName\": \"Test Product\", \"address\": \"Test Address\"}"))
                .andExpect(status().isOk());
    }

    // ========== Error Response Format Tests ==========

    @Test
    @DisplayName("All error responses should have status field")
    void testErrorResponse_HasStatus() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status").isNumber());
    }

    @Test
    @DisplayName("All error responses should have message field")
    void testErrorResponse_HasMessage() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("All error responses should have timestamp field")
    void testErrorResponse_HasTimestamp() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Error responses should be JSON format")
    void testErrorResponse_IsJson() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ========== HTTP Status Code Tests ==========

    @Test
    @DisplayName("404 should be used for not found errors")
    void testHttpStatus_404ForNotFound() throws Exception {
        mockMvc.perform(get("/test/order-not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 should be used for bad request errors")
    void testHttpStatus_400ForBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-status-transition"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("403 should be used for forbidden errors")
    void testHttpStatus_403ForForbidden() throws Exception {
        mockMvc.perform(get("/test/jastiper-cannot-buy-self"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("500 should be used for server errors")
    void testHttpStatus_500ForServerError() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError());
    }
}


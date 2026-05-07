package id.ac.ui.cs.advprog.order.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CheckoutRequestValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_hasNoViolations() {
        CheckoutRequest request = buildValidRequest();

        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void missingProductId_hasViolations() {
        CheckoutRequest request = buildValidRequest();
        request.setProductId(" ");

        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void fractionalSubtotal_hasViolations() {
        CheckoutRequest request = buildValidRequest();
        request.setSubtotal(new BigDecimal("200000.50"));

        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    private CheckoutRequest buildValidRequest() {
        CheckoutRequest request = new CheckoutRequest();
        request.setProductId("PROD-001");
        request.setProductName("KitKat Matcha");
        request.setTitiperUserId("titiper-1");
        request.setJastiperUserId("jastiper-1");
        request.setQuantity(2);
        request.setSubtotal(new BigDecimal("200000"));
        request.setShippingAddress("Depok");
        return request;
    }
}


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

class OrderCreateRequestValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_hasNoViolations() {
        OrderCreateRequest request = buildValidRequest();

        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void missingProductId_hasViolations() {
        OrderCreateRequest request = buildValidRequest();
        request.setProductId(" ");

        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void negativeTotalPrice_hasViolations() {
        OrderCreateRequest request = buildValidRequest();
        request.setTotalPrice(new BigDecimal("-1"));

        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void fractionalTotalPrice_hasViolations() {
        OrderCreateRequest request = buildValidRequest();
        request.setTotalPrice(new BigDecimal("1000.50"));

        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    private OrderCreateRequest buildValidRequest() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId("PROD-001");
        request.setProductName("AirTag");
        request.setTitiperUserId("titiper-1");
        request.setJastiperUserId("jastiper-1");
        request.setQuantity(2);
        request.setTotalPrice(new BigDecimal("50000"));
        request.setShippingAddress("Depok");
        return request;
    }
}


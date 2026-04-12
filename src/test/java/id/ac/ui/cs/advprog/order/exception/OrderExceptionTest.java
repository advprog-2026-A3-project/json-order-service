package id.ac.ui.cs.advprog.order.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Custom Exception Classes
 * Tests: Message formatting, constructors, inheritance
 */
@DisplayName("Custom Exception Classes Tests")
class OrderExceptionTest {

    // ========== OrderNotFoundException Tests ==========

    @Test
    @DisplayName("OrderNotFoundException should be created with order ID")
    void testOrderNotFoundException_WithId() {
        OrderNotFoundException exception = new OrderNotFoundException(123L);
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("123"));
    }

    @Test
    @DisplayName("OrderNotFoundException should inherit from Exception")
    void testOrderNotFoundException_IsException() {
        OrderNotFoundException exception = new OrderNotFoundException(123L);
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("OrderNotFoundException message should be meaningful")
    void testOrderNotFoundException_HasMeaningfulMessage() {
        OrderNotFoundException exception = new OrderNotFoundException(999L);
        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }

    @Test
    @DisplayName("OrderNotFoundException should be throwable")
    void testOrderNotFoundException_CanBeThrown() {
        assertThrows(OrderNotFoundException.class, () -> {
            throw new OrderNotFoundException(1L);
        });
    }

    // ========== InvalidStatusTransitionException Tests ==========

    @Test
    @DisplayName("InvalidStatusTransitionException should be created with from and to status")
    void testInvalidStatusTransitionException_WithStatuses() {
        InvalidStatusTransitionException exception =
                new InvalidStatusTransitionException("PENDING", "COMPLETED");
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("PENDING"));
        assertTrue(exception.getMessage().contains("COMPLETED"));
    }

    @Test
    @DisplayName("InvalidStatusTransitionException should inherit from Exception")
    void testInvalidStatusTransitionException_IsException() {
        InvalidStatusTransitionException exception =
                new InvalidStatusTransitionException("PENDING", "COMPLETED");
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("InvalidStatusTransitionException message should show both statuses")
    void testInvalidStatusTransitionException_ShowsBothStatuses() {
        InvalidStatusTransitionException exception =
                new InvalidStatusTransitionException("PAID", "PENDING");
        String message = exception.getMessage();
        assertTrue(message.contains("PAID"));
        assertTrue(message.contains("PENDING"));
    }

    @Test
    @DisplayName("InvalidStatusTransitionException should be throwable")
    void testInvalidStatusTransitionException_CanBeThrown() {
        assertThrows(InvalidStatusTransitionException.class, () -> {
            throw new InvalidStatusTransitionException("A", "B");
        });
    }

    // ========== CannotCancelOrderException Tests ==========

    @Test
    @DisplayName("CannotCancelOrderException should be created with order ID and reason")
    void testCannotCancelOrderException_WithIdAndReason() {
        CannotCancelOrderException exception =
                new CannotCancelOrderException(1L, "Already shipped");
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    @DisplayName("CannotCancelOrderException should inherit from Exception")
    void testCannotCancelOrderException_IsException() {
        CannotCancelOrderException exception =
                new CannotCancelOrderException(1L, "Already shipped");
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("CannotCancelOrderException should be throwable")
    void testCannotCancelOrderException_CanBeThrown() {
        assertThrows(CannotCancelOrderException.class, () -> {
            throw new CannotCancelOrderException(1L, "Reason");
        });
    }

    // ========== CannotRateOrderException Tests ==========

    @Test
    @DisplayName("CannotRateOrderException should be created with order ID and reason")
    void testCannotRateOrderException_WithIdAndReason() {
        CannotRateOrderException exception =
                new CannotRateOrderException(1L, "Not completed");
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    @DisplayName("CannotRateOrderException should inherit from Exception")
    void testCannotRateOrderException_IsException() {
        CannotRateOrderException exception =
                new CannotRateOrderException(1L, "Not completed");
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("CannotRateOrderException should be throwable")
    void testCannotRateOrderException_CanBeThrown() {
        assertThrows(CannotRateOrderException.class, () -> {
            throw new CannotRateOrderException(1L, "Reason");
        });
    }

    // ========== JastipperCannotBuySelfException Tests ==========

    @Test
    @DisplayName("JastipperCannotBuySelfException should be created with user ID")
    void testJastipperCannotBuySelfException_WithUserId() {
        JastipperCannotBuySelfException exception =
                new JastipperCannotBuySelfException("jastiper-001");
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("jastiper-001"));
    }

    @Test
    @DisplayName("JastipperCannotBuySelfException should inherit from Exception")
    void testJastipperCannotBuySelfException_IsException() {
        JastipperCannotBuySelfException exception =
                new JastipperCannotBuySelfException("jastiper-001");
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("JastipperCannotBuySelfException should be throwable")
    void testJastipperCannotBuySelfException_CanBeThrown() {
        assertThrows(JastipperCannotBuySelfException.class, () -> {
            throw new JastipperCannotBuySelfException("user-id");
        });
    }

    // ========== OrderException Tests ==========

    @Test
    @DisplayName("OrderException should be created with message")
    void testOrderException_WithMessage() {
        OrderException exception = new OrderException("Generic error");
        assertNotNull(exception);
        assertEquals("Generic error", exception.getMessage());
    }

    @Test
    @DisplayName("OrderException should inherit from Exception")
    void testOrderException_IsException() {
        OrderException exception = new OrderException("Error");
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("OrderException should be throwable")
    void testOrderException_CanBeThrown() {
        assertThrows(OrderException.class, () -> {
            throw new OrderException("Test");
        });
    }

    @Test
    @DisplayName("OrderException can be created with message and cause")
    void testOrderException_WithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        OrderException exception = new OrderException("Wrapper", cause);
        assertNotNull(exception);
        assertNotNull(exception.getCause());
    }

    // ========== Exception Message Tests ==========

    @Test
    @DisplayName("All exceptions should have non-empty messages")
    void testAllExceptions_HaveMessages() {
        assertFalse(new OrderNotFoundException(1L).getMessage().isEmpty());
        assertFalse(new InvalidStatusTransitionException("A", "B").getMessage().isEmpty());
        assertFalse(new CannotCancelOrderException(1L, "Reason").getMessage().isEmpty());
        assertFalse(new CannotRateOrderException(1L, "Reason").getMessage().isEmpty());
        assertFalse(new JastipperCannotBuySelfException("user").getMessage().isEmpty());
        assertFalse(new OrderException("Error").getMessage().isEmpty());
    }

    // ========== Exception Chaining Tests ==========

    @Test
    @DisplayName("Exceptions should support cause chaining")
    void testExceptions_SupportCauseChaining() {
        Throwable cause = new RuntimeException("Original error");
        OrderException exception = new OrderException("Wrapped error", cause);
        assertEquals(cause, exception.getCause());
    }

    // ========== Constructor Variations Tests ==========

    @Test
    @DisplayName("OrderException can be created with just message")
    void testOrderException_MessageOnly() {
        OrderException exception = new OrderException("Test message");
        assertEquals("Test message", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("OrderException can be created with message and cause")
    void testOrderException_MessageAndCause() {
        Throwable cause = new IllegalArgumentException("Invalid");
        OrderException exception = new OrderException("Error", cause);
        assertEquals("Error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}


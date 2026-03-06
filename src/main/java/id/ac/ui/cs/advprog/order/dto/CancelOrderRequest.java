package id.ac.ui.cs.advprog.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for cancelling an order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}


package id.ac.ui.cs.advprog.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for rating information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
    private Long id;
    private Long orderId;
    private String jastiperUserId;
    private String productId;
    private Integer ratingValue;
    private String review;
    private Boolean ratedByTitiper;
    private LocalDateTime createdAt;
}


package id.ac.ui.cs.advprog.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "jastiper_id")
    private String jastiperUserId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "rating_value", nullable = false)
    @Min(1)
    @Max(5)
    private Integer ratingValue;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "rated_by_titiper", nullable = false)
    private Boolean ratedByTitiper;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (ratedByTitiper == null) {
            ratedByTitiper = true;
        }
    }
}


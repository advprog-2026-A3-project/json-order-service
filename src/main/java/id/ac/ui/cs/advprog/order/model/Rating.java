package id.ac.ui.cs.advprog.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "jastiper_id")
    private String jastiperId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "rating_value", nullable = false)
    private Integer ratingValue;

    @Column(name = "review")
    private String review;

    @Column(name = "rated_by_titiper", nullable = false)
    private Boolean ratedByTitiper;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getJastiperId() {
        return jastiperId;
    }

    public void setJastiperId(String jastiperId) {
        this.jastiperId = jastiperId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Boolean getRatedByTitiper() {
        return ratedByTitiper;
    }

    public void setRatedByTitiper(Boolean ratedByTitiper) {
        this.ratedByTitiper = ratedByTitiper;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (ratedByTitiper == null) {
            ratedByTitiper = Boolean.TRUE;
        }
        createdAt = LocalDateTime.now();
    }
}


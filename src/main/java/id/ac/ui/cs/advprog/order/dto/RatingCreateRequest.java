package id.ac.ui.cs.advprog.order.dto;

public class RatingCreateRequest {
    private Integer ratingValue;
    private String review;

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
}


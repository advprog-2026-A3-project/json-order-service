package id.ac.ui.cs.advprog.order.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RatingCreateRequestTest {

    @Test
    void gettersAndSetters_workAsExpected() {
        RatingCreateRequest request = new RatingCreateRequest();
        request.setRatingValue(4);
        request.setReview("Cepat dan sesuai");

        assertEquals(4, request.getRatingValue());
        assertEquals("Cepat dan sesuai", request.getReview());
    }
}


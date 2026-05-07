package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.RatingCreateRequest;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RatingControllerTest {

    private RatingService ratingService;
    private RatingController ratingController;

    @BeforeEach
    void setUp() {
        ratingService = mock(RatingService.class);
        ratingController = new RatingController(ratingService);
    }

    @Test
    void createRating_returnsCreatedResponse() {
        RatingCreateRequest request = new RatingCreateRequest();
        request.setRatingValue(5);
        request.setReview("Mantap");

        Rating rating = new Rating();
        rating.setOrderId(12L);
        when(ratingService.createRating(12L, request)).thenReturn(rating);

        ResponseEntity<Rating> response = ratingController.createRating(12L, request);

        verify(ratingService).createRating(12L, request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(rating, response.getBody());
    }
}

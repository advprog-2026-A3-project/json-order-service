package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.RatingCreateRequest;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RatingControllerTest {

    private RatingService ratingService;
    private RatingController ratingController;

    @BeforeEach
    void setUp() {
        ratingService = mock(RatingService.class);
        ratingController = new RatingController(ratingService);
    }

    @Test
    void createRating_redirectsToListWithViewerAndSuccessMessage() {
        RatingCreateRequest request = new RatingCreateRequest();
        request.setRatingValue(5);
        request.setReview("Mantap");

        String redirect = ratingController.createRating(12L, request, "titiper");

        verify(ratingService).createRating(12L, request);
        assertEquals("redirect:/order/list?viewer=titiper&success=Rating+berhasil+disimpan", redirect);
    }
}


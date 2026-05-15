package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.order.exception.RatingNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RatingControllerRestTest {

    private MockMvc mockMvc;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = mock(RatingService.class);
        RatingController controller = new RatingController(ratingService);

        Rating rating = new Rating();
        rating.setOrderId(1L);
        when(ratingService.createRating(any(Long.class), any())).thenReturn(rating);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postRating_returnsCreatedResponse() throws Exception {
        mockMvc.perform(post("/api/v1/orders/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "ratingValue": 5,
                              "review": "Cepat dan sesuai"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void postRating_whenNotAllowed_returnsBadRequest() throws Exception {
        doThrow(new RatingNotAllowedException("Rating hanya bisa dibuat untuk order COMPLETED"))
                .when(ratingService).createRating(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/api/v1/orders/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "ratingValue": 5,
                              "review": "Cepat dan sesuai"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating hanya bisa dibuat untuk order COMPLETED"));
    }
}

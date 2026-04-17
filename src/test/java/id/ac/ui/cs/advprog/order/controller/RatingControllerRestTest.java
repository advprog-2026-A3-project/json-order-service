package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.exception.RatingNotAllowedException;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RatingControllerRestTest {

    private MockMvc mockMvc;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = mock(RatingService.class);
        RatingController controller = new RatingController(ratingService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new OrderExceptionHandler())
                .build();
    }

    @Test
    void postRating_redirectsToSuccessMessage() throws Exception {
        mockMvc.perform(post("/order/1/rating")
                        .param("viewer", "titiper")
                        .param("ratingValue", "5")
                        .param("review", "Cepat dan sesuai"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?viewer=titiper&success=Rating+berhasil+disimpan"));
    }

    @Test
    void postRating_whenNotAllowed_redirectsToErrorFromAdvice() throws Exception {
        doThrow(new RatingNotAllowedException("Rating hanya bisa dibuat untuk order COMPLETED"))
                .when(ratingService).createRating(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/order/1/rating")
                        .param("viewer", "titiper")
                        .param("ratingValue", "5")
                        .param("review", "Cepat dan sesuai"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list?error=Rating+hanya+bisa+dibuat+untuk+order+COMPLETED"));
    }
}


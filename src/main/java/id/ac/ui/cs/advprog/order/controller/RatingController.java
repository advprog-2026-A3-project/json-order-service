package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.RatingCreateRequest;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<Rating> createRating(
        @PathVariable("id") Long orderId,
        @Valid @RequestBody RatingCreateRequest request
    ) {
        Rating rating = ratingService.createRating(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }
}

package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.RatingCreateRequest;
import id.ac.ui.cs.advprog.order.model.Rating;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RatingService {
    Rating createRating(Long orderId, RatingCreateRequest request);

    Optional<Rating> getRatingByOrderId(Long orderId);

    Map<Long, Rating> getRatingsByOrderIds(List<Long> orderIds);
}


package id.ac.ui.cs.advprog.order.service;

import id.ac.ui.cs.advprog.order.dto.RatingCreateRequest;
import id.ac.ui.cs.advprog.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.order.exception.RatingAlreadyExistsException;
import id.ac.ui.cs.advprog.order.exception.RatingNotAllowedException;
import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import id.ac.ui.cs.advprog.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.order.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, OrderRepository orderRepository) {
        this.ratingRepository = ratingRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Rating createRating(Long orderId, RatingCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RatingNotAllowedException("Rating hanya bisa dibuat untuk order COMPLETED");
        }

        Integer ratingValue = request.getRatingValue();
        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            throw new RatingNotAllowedException("Nilai rating harus di antara 1 sampai 5");
        }

        if (ratingRepository.existsByOrderId(orderId)) {
            throw new RatingAlreadyExistsException(orderId);
        }

        Rating rating = new Rating();
        rating.setOrderId(orderId);
        rating.setJastiperId(order.getJastiperUserId());
        rating.setProductId(order.getProductId());
        rating.setRatingValue(ratingValue);
        rating.setReview(request.getReview());
        rating.setRatedByTitiper(Boolean.TRUE);
        return ratingRepository.save(rating);
    }

    @Override
    public Optional<Rating> getRatingByOrderId(Long orderId) {
        return ratingRepository.findByOrderId(orderId);
    }

    @Override
    public Map<Long, Rating> getRatingsByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return ratingRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(Rating::getOrderId, rating -> rating));
    }
}


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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Order completedOrder;
    private RatingCreateRequest request;

    @BeforeEach
    void setUp() {
        completedOrder = new Order();
        completedOrder.setId(1L);
        completedOrder.setProductId("PROD-1");
        completedOrder.setJastiperUserId("jastiper-1");
        completedOrder.setStatus(OrderStatus.COMPLETED);

        request = new RatingCreateRequest();
        request.setRatingValue(5);
        request.setReview("Produk sesuai ekspektasi");
    }

    @Test
    void createRating_successWhenOrderCompletedAndNotRated() {
        Rating saved = new Rating();
        saved.setOrderId(1L);
        saved.setRatingValue(5);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));
        when(ratingRepository.existsByOrderId(1L)).thenReturn(false);
        when(ratingRepository.save(org.mockito.ArgumentMatchers.any(Rating.class))).thenReturn(saved);

        Rating result = ratingService.createRating(1L, request);

        assertEquals(1L, result.getOrderId());
        assertEquals(5, result.getRatingValue());
        verify(ratingRepository).save(org.mockito.ArgumentMatchers.any(Rating.class));
    }

    @Test
    void createRating_failsWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> ratingService.createRating(99L, request));
        verify(ratingRepository, never()).save(org.mockito.ArgumentMatchers.any(Rating.class));
    }

    @Test
    void createRating_failsWhenOrderNotCompleted() {
        completedOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));

        assertThrows(RatingNotAllowedException.class, () -> ratingService.createRating(1L, request));
        verify(ratingRepository, never()).save(org.mockito.ArgumentMatchers.any(Rating.class));
    }

    @Test
    void createRating_failsWhenRatingValueOutOfRange() {
        request.setRatingValue(6);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));

        assertThrows(RatingNotAllowedException.class, () -> ratingService.createRating(1L, request));
        verify(ratingRepository, never()).save(org.mockito.ArgumentMatchers.any(Rating.class));
    }

    @Test
    void createRating_failsWhenAlreadyRated() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));
        when(ratingRepository.existsByOrderId(1L)).thenReturn(true);

        assertThrows(RatingAlreadyExistsException.class, () -> ratingService.createRating(1L, request));
        verify(ratingRepository, never()).save(org.mockito.ArgumentMatchers.any(Rating.class));
    }

    @Test
    void getRatingsByOrderIds_returnsMappedByOrderId() {
        Rating rating = new Rating();
        rating.setOrderId(1L);
        rating.setRatingValue(4);
        when(ratingRepository.findAllByOrderIdIn(List.of(1L))).thenReturn(List.of(rating));

        Map<Long, Rating> result = ratingService.getRatingsByOrderIds(List.of(1L));

        assertEquals(1, result.size());
        assertEquals(4, result.get(1L).getRatingValue());
    }
}


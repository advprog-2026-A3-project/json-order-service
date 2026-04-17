package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Order;
import id.ac.ui.cs.advprog.order.model.OrderStatus;
import id.ac.ui.cs.advprog.order.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RatingRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    void cleanUp() {
        ratingRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void saveAndFindByOrderId_returnsStoredRating() {
        Order order = createCompletedOrder();
        Order savedOrder = orderRepository.save(order);

        Rating rating = new Rating();
        rating.setOrderId(savedOrder.getId());
        rating.setProductId(savedOrder.getProductId());
        rating.setJastiperId(savedOrder.getJastiperUserId());
        rating.setRatingValue(5);
        rating.setReview("Mantap");
        ratingRepository.save(rating);

        Optional<Rating> result = ratingRepository.findByOrderId(savedOrder.getId());

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getRatingValue());
    }

    @Test
    void existsByOrderId_returnsTrueWhenRatingExists() {
        Order savedOrder = orderRepository.save(createCompletedOrder());

        Rating rating = new Rating();
        rating.setOrderId(savedOrder.getId());
        rating.setProductId(savedOrder.getProductId());
        rating.setJastiperId(savedOrder.getJastiperUserId());
        rating.setRatingValue(4);
        ratingRepository.save(rating);

        boolean exists = ratingRepository.existsByOrderId(savedOrder.getId());

        assertTrue(exists);
    }

    private Order createCompletedOrder() {
        Order order = new Order();
        order.setProductId("PROD-RATE");
        order.setProductName("Produk rating");
        order.setTitiperUserId("titiper-a");
        order.setJastiperUserId("jastiper-a");
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("25000"));
        order.setShippingAddress("Depok");
        order.setStatus(OrderStatus.COMPLETED);
        return order;
    }
}

package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RatingRepository.
 * Tests query methods for finding and aggregating ratings.
 */
@DataJpaTest
class RatingRepositoryIntegrationTest {

    @Autowired
    private RatingRepository ratingRepository;

    private Rating rating1;
    private Rating rating2;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();

        rating1 = Rating.builder()
                .orderId(1L)
                .jastiperUserId("jastiper-001")
                .productId("PROD-001")
                .ratingValue(5)
                .review("Excellent product!")
                .ratedByTitiper(true)
                .build();

        rating2 = Rating.builder()
                .orderId(2L)
                .jastiperUserId("jastiper-001")
                .productId("PROD-002")
                .ratingValue(4)
                .review("Good quality")
                .ratedByTitiper(true)
                .build();
    }

    @Test
    void testSaveAndFindRating() {
        Rating savedRating = ratingRepository.save(rating1);

        assertThat(savedRating.getId()).isNotNull();
        assertThat(ratingRepository.findById(savedRating.getId())).isPresent();
    }

    @Test
    void testFindByOrderId() {
        ratingRepository.save(rating1);

        List<Rating> result = ratingRepository.findByOrderId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
    }

    @Test
    void testFindByJastiperUserId() {
        ratingRepository.save(rating1);
        ratingRepository.save(rating2);

        List<Rating> result = ratingRepository.findByJastiperUserId("jastiper-001");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getJastiperUserId().equals("jastiper-001"));
    }

    @Test
    void testFindByProductId() {
        ratingRepository.save(rating1);
        ratingRepository.save(rating2);

        List<Rating> result = ratingRepository.findByProductId("PROD-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo("PROD-001");
    }

    @Test
    void testFindByOrderIdAndJastiperUserId() {
        ratingRepository.save(rating1);

        Optional<Rating> result = ratingRepository.findByOrderIdAndJastiperUserId(1L, "jastiper-001");

        assertThat(result).isPresent();
        assertThat(result.get().getRatingValue()).isEqualTo(5);
    }

    @Test
    void testFindByOrderIdAndProductId() {
        ratingRepository.save(rating1);

        Optional<Rating> result = ratingRepository.findByOrderIdAndProductId(1L, "PROD-001");

        assertThat(result).isPresent();
        assertThat(result.get().getReview()).isEqualTo("Excellent product!");
    }

    @Test
    void testGetAverageRatingForJastiper() {
        ratingRepository.save(rating1); // Rating: 5
        ratingRepository.save(rating2); // Rating: 4

        Optional<Double> result = ratingRepository.getAverageRatingForJastiper("jastiper-001");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(4.5);
    }

    @Test
    void testGetAverageRatingForProduct() {
        rating1.setProductId("PROD-001");
        rating2.setProductId("PROD-001");
        rating2.setRatingValue(3);

        ratingRepository.save(rating1); // Rating: 5
        ratingRepository.save(rating2); // Rating: 3

        Optional<Double> result = ratingRepository.getAverageRatingForProduct("PROD-001");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(4.0);
    }

    @Test
    void testRatingTimestamp() {
        Rating savedRating = ratingRepository.save(rating1);

        assertThat(savedRating.getCreatedAt()).isNotNull();
    }

    @Test
    void testRatingValidationConstraints() {
        rating1.setRatingValue(5);
        Rating savedRating = ratingRepository.save(rating1);

        assertThat(savedRating.getRatingValue()).isBetween(1, 5);
    }
}


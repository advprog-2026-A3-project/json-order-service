package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Find all ratings for a specific order
     */
    List<Rating> findByOrderId(Long orderId);

    /**
     * Find all ratings received by a Jastiper
     */
    List<Rating> findByJastiperUserId(String jastiperUserId);

    /**
     * Find all ratings for a specific product
     */
    List<Rating> findByProductId(String productId);

    /**
     * Find a rating by order ID and Jastiper ID
     */
    Optional<Rating> findByOrderIdAndJastiperUserId(Long orderId, String jastiperUserId);

    /**
     * Find a rating by order ID and product ID
     */
    Optional<Rating> findByOrderIdAndProductId(Long orderId, String productId);

    /**
     * Get average rating for a Jastiper
     */
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.jastiperUserId = :jastiperUserId")
    Optional<Double> getAverageRatingForJastiper(@Param("jastiperUserId") String jastiperUserId);

    /**
     * Get average rating for a product
     */
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.productId = :productId")
    Optional<Double> getAverageRatingForProduct(@Param("productId") String productId);
}


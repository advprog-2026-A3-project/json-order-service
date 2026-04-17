package id.ac.ui.cs.advprog.order.repository;

import id.ac.ui.cs.advprog.order.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<Rating> findAllByOrderIdIn(List<Long> orderIds);
}


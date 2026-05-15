package id.ac.ui.cs.advprog.order.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RatingTest {

    @Test
    void onCreate_setsDefaultRatedByTitiperAndCreatedAt() {
        Rating rating = new Rating();

        rating.onCreate();

        assertEquals(Boolean.TRUE, rating.getRatedByTitiper());
        assertNotNull(rating.getCreatedAt());
    }

    @Test
    void onCreate_keepsExistingRatedByTitiper() {
        Rating rating = new Rating();
        rating.setRatedByTitiper(Boolean.FALSE);

        rating.onCreate();

        assertEquals(Boolean.FALSE, rating.getRatedByTitiper());
        assertNotNull(rating.getCreatedAt());
    }
}


package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group3.computer.sba301_computershop.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductProductId(int productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") int productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId")
    Long getTotalReviewsByProductId(@Param("productId") int productId);
}

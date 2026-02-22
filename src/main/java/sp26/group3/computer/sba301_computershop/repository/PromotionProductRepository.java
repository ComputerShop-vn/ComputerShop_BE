package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group3.computer.sba301_computershop.entity.PromotionProduct;

import java.util.List;
import java.util.Optional;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Integer> {

    List<PromotionProduct> findByProductProductId(int productId);

    @Query("SELECT pp FROM PromotionProduct pp " +
            "JOIN FETCH pp.promotion p " +
            "WHERE pp.product.productId = :productId " +
            "AND p.startDate <= CURRENT_DATE " +
            "AND p.endDate >= CURRENT_DATE " +
            "ORDER BY p.discountPercent DESC")
    Optional<PromotionProduct> findActivePromotionByProductId(@Param("productId") int productId);
}

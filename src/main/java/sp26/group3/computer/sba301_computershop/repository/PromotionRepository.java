package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.Promotion;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    boolean existsByPromoCode(String promoCode);
    Optional<Promotion> findByPromoCode(String promoCode);
}
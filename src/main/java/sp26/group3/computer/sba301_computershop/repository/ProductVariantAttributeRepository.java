package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.ProductVariantAttribute;

import java.util.List;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, Integer> {

    List<ProductVariantAttribute> findByVariantVariantId(int variantId);

    void deleteByVariantVariantId(int variantId);
}

package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductProductId(int productId);

    Optional<ProductImage> findFirstByProductProductIdAndIsThumbnailTrue(int productId);

    void deleteByProductProductId(int productId);
}

package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group3.computer.sba301_computershop.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoryCategoryId(int categoryId);

    List<Product> findByBrandBrandId(int brandId);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Query("SELECT p FROM Product p WHERE "
            + "(:categoryId IS NULL OR p.category.categoryId = :categoryId) "
            + "AND (:brandId IS NULL OR p.brand.brandId = :brandId) "
            + "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) "
            + "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    List<Product> filterProducts(
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
}

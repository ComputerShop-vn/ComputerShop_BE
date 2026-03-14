package sp26.group3.computer.sba301_computershop.service;

import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductResponse createProduct(ProductCreationRequest request, MultipartFile[] images);

    ProductResponse updateProduct(int productId, ProductUpdateRequest request, MultipartFile[] images);

    ProductDetailResponse getProductById(int productId);

    List<ProductResponse> searchProducts(String keyword);

    /**
     * @param attributes Map of attributeName → requiredValue (case-insensitive).
     *                   A product passes if it has at least one variant satisfying ALL conditions.
     */
    List<ProductResponse> filterProducts(Integer categoryId, Integer brandId, Double minPrice, Double maxPrice,
                                         Map<String, String> attributes);

    void deleteProduct(int productId);
}

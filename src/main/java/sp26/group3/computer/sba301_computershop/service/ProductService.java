package sp26.group3.computer.sba301_computershop.service;

import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreationRequest request, MultipartFile[] images);

    ProductResponse updateProduct(int productId, ProductUpdateRequest request, MultipartFile[] images);

    ProductDetailResponse getProductById(int productId);

    List<ProductResponse> searchProducts(String keyword);

    List<ProductResponse> filterProducts(Integer categoryId, Integer brandId, Double minPrice, Double maxPrice);

    void deleteProduct(int productId);
}

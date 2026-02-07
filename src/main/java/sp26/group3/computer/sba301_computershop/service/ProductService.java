package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse createProduct(ProductCreationRequest request);
}

package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.ProductVariantCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductVariantUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductVariantResponse;
import sp26.group3.computer.sba301_computershop.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariant toProductVariant(ProductVariantCreationRequest request);

    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateProductVariant(@MappingTarget ProductVariant variant, ProductVariantUpdateRequest request);

    @Mapping(source = "product.productId", target = "productId")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    ProductVariantResponse toProductVariantResponse(ProductVariant variant);
}

package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "basePrice", ignore = true)
    Product toProduct(ProductCreationRequest request);

    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "brand.brandId", target = "brandId")
    @Mapping(source = "brand.brandName", target = "brandName")
    @Mapping(source = "brand.logoUrl", target = "brandLogoUrl")
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "variants", ignore = true)
    ProductResponse toProductResponse(Product product);

    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "brand.brandId", target = "brandId")
    @Mapping(source = "brand.brandName", target = "brandName")
    @Mapping(source = "brand.logoUrl", target = "brandLogoUrl")
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "hasPromotion", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "variants", ignore = true)
    ProductDetailResponse toProductDetailResponse(Product product);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "basePrice", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}

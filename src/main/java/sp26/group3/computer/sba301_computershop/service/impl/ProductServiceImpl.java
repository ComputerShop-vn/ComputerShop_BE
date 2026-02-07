package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import sp26.group3.computer.sba301_computershop.dto.request.ProductAttributeRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductAttributeResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.enums.ProductItemStatus;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoriesRepository categoriesRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductItemRepository productItemRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreationRequest request) {
        // 1. Validate Brand and Category
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + request.getBrandId()));

        Categories category = categoriesRepository.findById(request.getCategoriesId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoriesId()));

        // 2. Determine Stock Quantity
        // Logic: specific rule "user have two option, manage by stockQuantity or
        // serialNumber, if stockQuantity is null, then list serialNumber is not null,
        // and vice versa"
        int stockQuantity = 0;
        if (!CollectionUtils.isEmpty(request.getSerialNumbers())) {
            stockQuantity = request.getSerialNumbers().size();
        } else if (request.getStockQuantity() != null) {
            stockQuantity = request.getStockQuantity();
        }
        // 3. Create and Save Product
        Product product = Product.builder()
                .brand(brand)
                .categories(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(stockQuantity)
                .build();

        Product savedProduct = productRepository.save(product);

        // 4. Handle Serial Numbers (ProductItems)
        if (!CollectionUtils.isEmpty(request.getSerialNumbers())) {
            // Only create items if serial numbers are provided
            List<ProductItem> productItems = request.getSerialNumbers().stream().map(sn -> {
                return ProductItem.builder()
                        .product(savedProduct)
                        .serialNumber(sn)
                        .status(ProductItemStatus.AVAILABLE)
                        .build();
            }).toList();
            productItemRepository.saveAll(productItems);
        }

        // 5. Handle Attributes
        List<ProductAttribute> productAttributes = new ArrayList<>();

        if (request.getAttributes() != null) {
            List<Attribute> attributes = attributeRepository
                    .findAllById(
                            request.getAttributes().stream().map(ProductAttributeRequest::getAttributeId).toList());
            if (attributes.size() != request.getAttributes().size()) {
                throw new RuntimeException("Attribute not found with ID: "
                        + request.getAttributes().stream().map(ProductAttributeRequest::getAttributeId).toList());
            }
            Map<Integer, String> attributeMap = request.getAttributes().stream()
                    .collect(Collectors.toMap(ProductAttributeRequest::getAttributeId,
                            ProductAttributeRequest::getAttributeValue));
            productAttributes = attributes.stream().map(attribute -> {
                return ProductAttribute.builder()
                        .product(savedProduct)
                        .attribute(attribute)
                        .value(attributeMap.get(attribute.getAttributeId()))
                        .build();
            }).toList();
            productAttributeRepository.saveAll(productAttributes);

        }

        // 6. Handle Images
        List<ProductImage> productImages = new ArrayList<>();
        boolean isFirst = true; // Biến này thay đổi được trong vòng for

        for (String url : request.getImageUrls()) {
            ProductImage img = ProductImage.builder()
                    .product(savedProduct)
                    .imageUrl(url)
                    .isThumbnail(isFirst)
                    .build();

            productImages.add(img);

            if (isFirst) {
                isFirst = false;
            }
        }

        productImageRepository.saveAll(productImages);
        List<ProductAttributeResponse> savedAttributes = productAttributes.stream()
                .map(attribute -> {
                    return ProductAttributeResponse.builder()
                            .attributeId(attribute.getAttribute().getAttributeId())
                            .attributeName(attribute.getAttribute().getAttributeName())
                            .attributeValue(attribute.getValue())
                            .build();
                }).toList();
        // 7. Construct Response
        return ProductResponse.builder()
                .productId(savedProduct.getProductId())
                .categoriesId(category.getCategoryId())
                .brandId(brand.getBrandId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .stockQuantity(savedProduct.getStockQuantity())
                .serialNumbers(request.getSerialNumbers())
                .attributes(savedAttributes)
                .images(request.getImageUrls())
                .build();
    }
}

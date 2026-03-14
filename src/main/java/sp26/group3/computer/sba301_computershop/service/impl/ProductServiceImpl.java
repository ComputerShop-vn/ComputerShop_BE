package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeValueDTO;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.request.VariantCreationDTO;
import sp26.group3.computer.sba301_computershop.dto.request.VariantUpdateDTO;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductVariantResponse;
import sp26.group3.computer.sba301_computershop.dto.response.VariantAttributeResponse;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.ProductMapper;
import sp26.group3.computer.sba301_computershop.mapper.ProductVariantMapper;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.CloudinaryService;
import sp26.group3.computer.sba301_computershop.service.ProductService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductImageRepository productImageRepository;
    ReviewRepository reviewRepository;
    PromotionProductRepository promotionProductRepository;
    ProductVariantRepository productVariantRepository;
    ProductVariantAttributeRepository productVariantAttributeRepository;
    AttributeRepository attributeRepository;
    CloudinaryService cloudinaryService;
    ProductMapper productMapper;
    ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreationRequest request, MultipartFile[] images) {
        log.info("Creating product with name: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        Product product = productMapper.toProduct(request);
        product.setCategory(category);
        product.setBrand(brand);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getProductId());

        if (images != null && images.length > 0) {
            List<String> imageUrls = cloudinaryService.uploadProductImages(images);
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductImage productImage = ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(imageUrls.get(i))
                        .isThumbnail(i == 0)
                        .build();
                productImageRepository.save(productImage);
            }
            log.info("Saved {} images for product id: {}", imageUrls.size(), savedProduct.getProductId());
        }

        // Create variants if provided
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            log.info("Creating {} variants for product id: {}", request.getVariants().size(),
                    savedProduct.getProductId());

            for (VariantCreationDTO variantDTO : request.getVariants()) {
                if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                    throw new AppException(ErrorCode.SKU_EXISTED);
                }

                ProductVariant variant = ProductVariant.builder()
                        .product(savedProduct)
                        .sku(variantDTO.getSku())
                        .price(variantDTO.getPrice())
                        .stockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : 0)
                        .variantName(variantDTO.getVariantName())
                        .build();

                ProductVariant savedVariant = productVariantRepository.save(variant);

                // Create variant attributes if provided
                if (variantDTO.getAttributes() != null && !variantDTO.getAttributes().isEmpty()) {
                    for (AttributeValueDTO attrDTO : variantDTO.getAttributes()) {
                        Attribute attribute = resolveOrCreateAttribute(attrDTO);

                        ProductVariantAttribute variantAttribute = ProductVariantAttribute.builder()
                                .variant(savedVariant)
                                .attribute(attribute)
                                .value(attrDTO.getValue())
                                .build();

                        productVariantAttributeRepository.save(variantAttribute);
                    }
                }
            }

            // Update product base price to the lowest variant price
            updateProductBasePrice(savedProduct.getProductId());

            log.info("Created {} variants for product id: {}", request.getVariants().size(),
                    savedProduct.getProductId());
        }

        ProductResponse response = productMapper.toProductResponse(savedProduct);
        populateVariants(response);
        populateThumbnail(response);
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(int productId, ProductUpdateRequest request, MultipartFile[] images) {
        log.info("Updating product with id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            product.setBrand(brand);
        }

        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", productId);

        if (images != null && images.length > 0) {
            List<ProductImage> existingImages = productImageRepository.findByProductProductId(productId);

            for (ProductImage img : existingImages) {
                cloudinaryService.deleteFile(img.getImageUrl());
            }

            productImageRepository.deleteByProductProductId(productId);

            List<String> imageUrls = cloudinaryService.uploadProductImages(images);
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductImage productImage = ProductImage.builder()
                        .product(updatedProduct)
                        .imageUrl(imageUrls.get(i))
                        .isThumbnail(i == 0)
                        .build();
                productImageRepository.save(productImage);
            }
            log.info("Updated {} images for product id: {}", imageUrls.size(), productId);
        }

        if (request.getVariants() != null) {
            List<VariantUpdateDTO> incomingVariants = request.getVariants();
            log.info("Processing {} variants for product id: {}", incomingVariants.size(), productId);

            // Collect variantIds còn tồn tại sau update
            Set<Integer> incomingIds = incomingVariants.stream()
                    .filter(v -> v.getVariantId() != null)
                    .map(VariantUpdateDTO::getVariantId)
                    .collect(Collectors.toSet());

            // Xóa các variant cũ không có trong danh sách gửi lên
            List<ProductVariant> existingVariants = productVariantRepository.findByProductProductId(productId);
            for (ProductVariant existing : existingVariants) {
                if (!incomingIds.contains(existing.getVariantId())) {
                    deleteVariant(existing.getVariantId());
                    log.info("Auto-deleted variant id: {}", existing.getVariantId());
                }
            }

            for (VariantUpdateDTO variantDTO : incomingVariants) {
                if (variantDTO.getVariantId() != null) {
                    updateExistingVariant(variantDTO);
                    log.info("Updated variant id: {}", variantDTO.getVariantId());
                } else {
                    createNewVariant(updatedProduct, variantDTO);
                    log.info("Created new variant with SKU: {}", variantDTO.getSku());
                }
            }

            updateProductBasePrice(productId);
        } else {
            log.info("Variants not provided for product id: {} — keeping existing variants", productId);
        }

        ProductResponse response = productMapper.toProductResponse(updatedProduct);
        populateVariants(response);
        populateThumbnail(response);
        return response;
    }

    @Override
    public ProductDetailResponse getProductById(int productId) {
        log.info("Getting product detail with id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductDetailResponse response = productMapper.toProductDetailResponse(product);

        List<ProductImage> productImages = productImageRepository.findByProductProductId(productId);
        List<String> imageUrls = productImages.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);

        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.getTotalReviewsByProductId(productId);
        response.setAverageRating(avgRating != null ? avgRating : 0.0);
        response.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);

        promotionProductRepository.findActivePromotionByProductId(productId)
                .stream().findFirst()
                .ifPresent(pp -> {
                    Promotion promo = pp.getPromotion();
                    response.setHasPromotion(true);
                    response.setDiscountPercent((double) promo.getDiscountPercent());
                    response.setPromoCode(promo.getPromoCode());
                });

        if (response.getHasPromotion() == null || !response.getHasPromotion()) {
            response.setHasPromotion(false);
            response.setDiscountPercent(0.0);
            response.setPromoCode(null);
        }

        if (response.getHasPromotion() && response.getDiscountPercent() != null &&
                response.getDiscountPercent() > 0 && response.getBasePrice() != null) {
            double discountedPrice = response.getBasePrice() * (1 - response.getDiscountPercent() / 100.0);
            response.setDiscountedPrice(discountedPrice);
        }

        populateVariants(response);

        log.info("Product detail populated with {} images, avgRating={}, totalReviews={}, hasPromotion={}, {} variants",
                imageUrls.size(), avgRating, totalReviews, response.getHasPromotion(),
                response.getVariants() != null ? response.getVariants().size() : 0);

        return response;
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);

        List<ProductResponse> responses = productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        responses.forEach(r -> {
            populateVariants(r);
            populateDiscountedPrice(r);
            populateThumbnail(r);
        });

        return responses;
    }

    @Override
    public List<ProductResponse> filterProducts(Integer categoryId, Integer brandId, Double minPrice, Double maxPrice,
                                                Map<String, String> attributes) {
        log.info("Filtering products - categoryId: {}, brandId: {}, minPrice: {}, maxPrice: {}, attributes: {}",
                categoryId, brandId, minPrice, maxPrice, attributes);

        List<Product> products = productRepository.filterProducts(categoryId, brandId, minPrice, maxPrice);

        // Attribute filter: giữ lại product có ít nhất 1 variant thỏa mãn TẤT CẢ điều kiện
        if (attributes != null && !attributes.isEmpty()) {
            products = products.stream()
                    .filter(p -> hasMatchingVariant(p.getProductId(), attributes))
                    .collect(Collectors.toList());
        }

        List<ProductResponse> responses = products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        responses.forEach(r -> {
            populateVariants(r);
            populateDiscountedPrice(r);
            populateThumbnail(r);
        });

        return responses;
    }

    /**
     * Trả true nếu product có ít nhất 1 variant mà TẤT CẢ attribute conditions đều khớp.
     *
     * Hỗ trợ 3 dạng filter value:
     *   "AM5"        → exact match (case-insensitive)
     *   "lte:400"    → numeric: variant attr value ≤ 400
     *   "gte:650"    → numeric: variant attr value ≥ 650
     */
    private boolean hasMatchingVariant(int productId, Map<String, String> conditions) {
        List<ProductVariant> variants = productVariantRepository.findByProductProductId(productId);
        for (ProductVariant variant : variants) {
            Map<String, String> variantAttrs = productVariantAttributeRepository
                    .findByVariantVariantId(variant.getVariantId())
                    .stream()
                    .collect(Collectors.toMap(
                            va -> va.getAttribute().getAttributeName().toLowerCase(),
                            va -> va.getValue(),
                            (a, b) -> a));
            boolean allMatch = conditions.entrySet().stream()
                    .allMatch(e -> matchesCondition(e.getKey(), e.getValue(), variantAttrs));
            if (allMatch) return true;
        }
        return false;
    }

    /**
     * So sánh 1 điều kiện với giá trị attribute của variant.
     * filterValue có thể là "AM5", "lte:400", hoặc "gte:650".
     */
    private boolean matchesCondition(String attrName, String filterValue, Map<String, String> variantAttrs) {
        String rawAttrValue = variantAttrs.get(attrName.toLowerCase());
        if (rawAttrValue == null) return false;

        String lowerFilter = filterValue.toLowerCase();
        if (lowerFilter.startsWith("lte:")) {
            try {
                double max = extractNumber(lowerFilter.substring(4).trim());
                return extractNumber(rawAttrValue) <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (lowerFilter.startsWith("gte:")) {
            try {
                double min = extractNumber(lowerFilter.substring(4).trim());
                return extractNumber(rawAttrValue) >= min;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return filterValue.equalsIgnoreCase(rawAttrValue);
        }
    }

    /** Trích con số đầu tiên từ string như "336mm", "6000MHz", "450W" → double. */
    private double extractNumber(String value) {
        String digits = value.replaceAll("[^0-9.]", "").trim();
        if (digits.isEmpty()) throw new NumberFormatException("No digits in: " + value);
        return Double.parseDouble(digits);
    }

    @Transactional
    public void deleteProduct(int productId) {
        log.warn("Deleting product with id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Delete variant attributes and variants
        List<ProductVariant> variants = productVariantRepository.findByProductProductId(productId);
        for (ProductVariant variant : variants) {
            productVariantAttributeRepository.deleteByVariantVariantId(variant.getVariantId());
        }
        productVariantRepository.deleteAll(variants);

        // Delete promotion associations
        promotionProductRepository.deleteByProductProductId(productId);

        // Delete images from Cloudinary and DB
        List<ProductImage> images = productImageRepository.findByProductProductId(productId);
        for (ProductImage img : images) {
            cloudinaryService.deleteFile(img.getImageUrl());
        }
        productImageRepository.deleteByProductProductId(productId);

        // Delete product
        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", productId);
    }

    private void populateDiscountedPrice(ProductResponse response) {
        if (response.getBasePrice() == null)
            return;

        promotionProductRepository.findActivePromotionByProductId(response.getProductId())
                .stream().findFirst()
                .ifPresent(pp -> {
                    Promotion promo = pp.getPromotion();
                    double discountPercent = promo.getDiscountPercent();
                    double discountedPrice = response.getBasePrice() * (1 - discountPercent / 100.0);
                    response.setDiscountedPrice(discountedPrice);
                });
    }

    private void populateThumbnail(ProductResponse response) {
        productImageRepository.findFirstByProductProductIdAndIsThumbnailTrue(response.getProductId())
                .ifPresent(img -> response.setThumbnailUrl(img.getImageUrl()));
    }

    private void populateVariants(ProductResponse response) {
        List<ProductVariant> variants = productVariantRepository
                .findByProductProductId(response.getProductId());
        List<ProductVariantResponse> variantResponses = variants.stream()
                .map(v -> toVariantResponseWithAttributes(v))
                .collect(Collectors.toList());
        response.setVariants(variantResponses);
    }

    private void populateVariants(ProductDetailResponse response) {
        List<ProductVariant> variants = productVariantRepository
                .findByProductProductId(response.getProductId());
        List<ProductVariantResponse> variantResponses = variants.stream()
                .map(v -> toVariantResponseWithAttributes(v))
                .collect(Collectors.toList());
        response.setVariants(variantResponses);
    }

    private ProductVariantResponse toVariantResponseWithAttributes(ProductVariant variant) {
        ProductVariantResponse res = productVariantMapper.toProductVariantResponse(variant);
        List<ProductVariantAttribute> attrs = productVariantAttributeRepository
                .findByVariantVariantId(variant.getVariantId());
        List<VariantAttributeResponse> attrList = attrs.stream()
                .map(a -> VariantAttributeResponse.builder()
                        .attributeId(a.getAttribute().getAttributeId())
                        .attributeName(a.getAttribute().getAttributeName())
                        .value(a.getValue())
                        .build())
                .collect(Collectors.toList());
        res.setAttributes(attrList);
        return res;
    }

    private void updateProductBasePrice(int productId) {
        Double minPrice = productVariantRepository.findMinPriceByProductId(productId);
        if (minPrice != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            product.setBasePrice(minPrice);
            productRepository.save(product);
            log.info("Updated base price for product {}: {}", productId, minPrice);
        }
    }

    private void createNewVariant(Product product, VariantUpdateDTO variantDTO) {
        if (variantDTO.getSku() != null && productVariantRepository.existsBySku(variantDTO.getSku())) {
            throw new AppException(ErrorCode.SKU_EXISTED);
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(variantDTO.getSku())
                .price(variantDTO.getPrice())
                .stockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : 0)
                .variantName(variantDTO.getVariantName())
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);

        // Create variant attributes
        if (variantDTO.getAttributes() != null && !variantDTO.getAttributes().isEmpty()) {
            for (AttributeValueDTO attrDTO : variantDTO.getAttributes()) {
                Attribute attribute = resolveOrCreateAttribute(attrDTO);

                ProductVariantAttribute variantAttribute = ProductVariantAttribute.builder()
                        .variant(savedVariant)
                        .attribute(attribute)
                        .value(attrDTO.getValue())
                        .build();

                productVariantAttributeRepository.save(variantAttribute);
            }
        }
    }

    private void updateExistingVariant(VariantUpdateDTO variantDTO) {
        ProductVariant variant = productVariantRepository.findById(variantDTO.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        // Check SKU uniqueness if changed
        if (variantDTO.getSku() != null && !variantDTO.getSku().equals(variant.getSku())) {
            if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                throw new AppException(ErrorCode.SKU_EXISTED);
            }
            variant.setSku(variantDTO.getSku());
        }

        if (variantDTO.getPrice() != null) {
            variant.setPrice(variantDTO.getPrice());
        }

        if (variantDTO.getStockQuantity() != null) {
            variant.setStockQuantity(variantDTO.getStockQuantity());
        }

        if (variantDTO.getVariantName() != null) {
            variant.setVariantName(variantDTO.getVariantName());
        }

        ProductVariant updatedVariant = productVariantRepository.save(variant);

        // null = không truyền → giữ nguyên attributes hiện có
        // [] = truyền mảng rỗng → xoá toàn bộ attributes
        // [...] = merge: giữ attribute đã có (theo attributeId), thêm mới, xoá
        // attribute không còn trong list
        if (variantDTO.getAttributes() != null) {
            List<ProductVariantAttribute> existingAttrs = productVariantAttributeRepository
                    .findByVariantVariantId(variantDTO.getVariantId());

            // Map attributeId → existing record
            Map<Integer, ProductVariantAttribute> existingByAttrId = new HashMap<>();
            for (ProductVariantAttribute existing : existingAttrs) {
                existingByAttrId.put(existing.getAttribute().getAttributeId(), existing);
            }

            Set<Integer> incomingAttrIds = new HashSet<>();
            for (AttributeValueDTO attrDTO : variantDTO.getAttributes()) {
                Attribute attribute = resolveOrCreateAttribute(attrDTO);
                int attrId = attribute.getAttributeId();
                incomingAttrIds.add(attrId);

                if (existingByAttrId.containsKey(attrId)) {
                    // Attribute đã tồn tại → chỉ update value nếu có thay đổi
                    ProductVariantAttribute existing = existingByAttrId.get(attrId);
                    if (attrDTO.getValue() != null && !attrDTO.getValue().equals(existing.getValue())) {
                        existing.setValue(attrDTO.getValue());
                        productVariantAttributeRepository.save(existing);
                    }
                } else {
                    // Attribute chưa có → tạo mới
                    ProductVariantAttribute variantAttribute = ProductVariantAttribute.builder()
                            .variant(updatedVariant)
                            .attribute(attribute)
                            .value(attrDTO.getValue())
                            .build();
                    productVariantAttributeRepository.save(variantAttribute);
                }
            }

            // Xoá các attribute cũ không có trong list gửi lên
            for (ProductVariantAttribute existing : existingAttrs) {
                if (!incomingAttrIds.contains(existing.getAttribute().getAttributeId())) {
                    productVariantAttributeRepository.delete(existing);
                }
            }
        }
    }

    private void deleteVariant(int variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        // Delete variant attributes first
        productVariantAttributeRepository.deleteByVariantVariantId(variantId);

        // Delete variant
        productVariantRepository.delete(variant);
    }

    private Attribute resolveOrCreateAttribute(AttributeValueDTO attrDTO) {
        if (attrDTO.getAttributeId() != null) {
            // Tìm theo ID
            return attributeRepository.findById(attrDTO.getAttributeId())
                    .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_NOT_FOUND));
        }
        // Tìm theo tên, nếu không tồn tại thì tạo mới
        String name = attrDTO.getAttributeName();
        if (name == null || name.isBlank()) {
            throw new AppException(ErrorCode.ATTRIBUTE_NOT_FOUND);
        }
        return attributeRepository.findByAttributeName(name)
                .orElseGet(() -> attributeRepository.save(
                        Attribute.builder().attributeName(name).build()));
    }
}
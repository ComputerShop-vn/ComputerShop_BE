package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.ProductMapper;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.CloudinaryService;
import sp26.group3.computer.sba301_computershop.service.ProductService;

import java.util.List;
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
    CloudinaryService cloudinaryService;
    ProductMapper productMapper;

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

        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        } else {
            product.setStockQuantity(0);
        }

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

        return productMapper.toProductResponse(savedProduct);
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

        productMapper.updateProduct(product, request);

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

        return productMapper.toProductResponse(updatedProduct);
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

        if (response.getHasPromotion() && response.getDiscountPercent() != null && response.getDiscountPercent() > 0) {
            double discountedPrice = response.getPrice() * (1 - response.getDiscountPercent() / 100.0);
            response.setDiscountedPrice(discountedPrice);
        }

        log.info("Product detail populated with {} images, avgRating={}, totalReviews={}, hasPromotion={}",
                imageUrls.size(), avgRating, totalReviews, response.getHasPromotion());

        return response;
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);

        List<ProductResponse> responses = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        responses.forEach(this::populateDiscountedPrice);

        return responses;
    }

    @Override
    public List<ProductResponse> filterProducts(Integer categoryId, Integer brandId, Double minPrice, Double maxPrice) {
        log.info("Filtering products - categoryId: {}, brandId: {}, minPrice: {}, maxPrice: {}",
                categoryId, brandId, minPrice, maxPrice);

        List<Product> products = productRepository.findAll();

        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory().getCategoryId() == categoryId)
                    .collect(Collectors.toList());
        }

        if (brandId != null) {
            products = products.stream()
                    .filter(p -> p.getBrand().getBrandId() == brandId)
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }

        List<ProductResponse> responses = products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        responses.forEach(this::populateDiscountedPrice);

        return responses;
    }

    @Transactional
    public void deleteProduct(int productId) {
        log.warn("Deleting product with id: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        List<ProductImage> images = productImageRepository.findByProductProductId(productId);
        for (ProductImage img : images) {
            cloudinaryService.deleteFile(img.getImageUrl());
        }
        productImageRepository.deleteByProductProductId(productId);
    }

    private void populateDiscountedPrice(ProductResponse response) {
        promotionProductRepository.findActivePromotionByProductId(response.getProductId())
                .ifPresent(pp -> {
                    Promotion promo = pp.getPromotion();
                    double discountPercent = promo.getDiscountPercent();
                    double discountedPrice = response.getPrice() * (1 - discountPercent / 100.0);
                    response.setDiscountedPrice(discountedPrice);
                });
    }
}
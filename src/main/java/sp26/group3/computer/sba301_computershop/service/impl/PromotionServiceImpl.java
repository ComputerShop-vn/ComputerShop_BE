package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PromotionResponse;
import sp26.group3.computer.sba301_computershop.entity.Promotion;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.PromotionMapper;
import sp26.group3.computer.sba301_computershop.repository.PromotionRepository;
import sp26.group3.computer.sba301_computershop.service.PromotionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;

    @Override
    public PromotionResponse createPromotion(PromotionCreationRequest request) {
        log.info("Creating promotion with code: {}", request.getPromoCode());

        // Check if promo code already exists
        if (promotionRepository.existsByPromoCode(request.getPromoCode())) {
            throw new AppException(ErrorCode.PROMO_CODE_EXISTED);
        }

        Promotion promotion = promotionMapper.toPromotion(request);
        Promotion savedPromotion = promotionRepository.save(promotion);

        log.info("Promotion created successfully with id: {}", savedPromotion.getPromotionId());

        return promotionMapper.toPromotionResponse(savedPromotion);
    }

    @Override
    public PromotionResponse updatePromotion(int promotionId, PromotionUpdateRequest request) {
        log.info("Updating promotion with id: {}", promotionId);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        // Check if promo code is being changed and if it already exists
        if (request.getPromoCode() != null &&
                !request.getPromoCode().equals(promotion.getPromoCode()) &&
                promotionRepository.existsByPromoCode(request.getPromoCode())) {
            throw new AppException(ErrorCode.PROMO_CODE_EXISTED);
        }

        promotionMapper.updatePromotion(promotion, request);
        Promotion updatedPromotion = promotionRepository.save(promotion);

        log.info("Promotion updated successfully with id: {}", promotionId);

        return promotionMapper.toPromotionResponse(updatedPromotion);
    }

    @Override
    public PromotionResponse getPromotionById(int promotionId) {
        log.info("Getting promotion with id: {}", promotionId);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PromotionResponse getPromotionByCode(String promoCode) {
        log.info("Getting promotion with code: {}", promoCode);

        Promotion promotion = promotionRepository.findByPromoCode(promoCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        log.info("Getting all promotions");

        return promotionRepository.findAll()
                .stream()
                .map(promotionMapper::toPromotionResponse)
                .toList();
    }

    @Override
    public void deletePromotion(int promotionId) {
        log.warn("Deleting promotion with id: {}", promotionId);

        if (!promotionRepository.existsById(promotionId)) {
            throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
        }

        promotionRepository.deleteById(promotionId);
        log.info("Promotion deleted successfully with id: {}", promotionId);
    }
}
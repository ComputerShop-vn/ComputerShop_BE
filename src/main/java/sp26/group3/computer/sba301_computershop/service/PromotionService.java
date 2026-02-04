package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.PromotionCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {

    PromotionResponse createPromotion(PromotionCreationRequest request);

    PromotionResponse updatePromotion(int promotionId, PromotionUpdateRequest request);

    PromotionResponse getPromotionById(int promotionId);

    PromotionResponse getPromotionByCode(String promoCode);

    List<PromotionResponse> getAllPromotions();

    void deletePromotion(int promotionId);
}
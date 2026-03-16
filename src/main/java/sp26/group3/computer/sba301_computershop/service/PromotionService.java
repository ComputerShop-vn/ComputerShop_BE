package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {

    PromotionResponse createPromotion(PromotionCreationRequest request);

    PromotionResponse updatePromotion(int promotionId, PromotionUpdateRequest request);

    PromotionResponse getPromotionById(int promotionId);

    PromotionResponse getPromotionByCode(String promoCode);

    List<PromotionResponse> getAllPromotions();

    PagedResponse<PromotionResponse> getAllPromotionsPaged(Pageable pageable);

    void deletePromotion(int promotionId);

    void addPromotionToProducts(int promotionId, List<Integer> productIds);

    void addPromotionToCategory(int promotionId, int categoryId);

    void addPromotionToBrand(int promotionId, int brandId);
}

package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PromotionResponse;
import sp26.group3.computer.sba301_computershop.entity.Promotion;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "promotionId", ignore = true)
    Promotion toPromotion(PromotionCreationRequest request);

    PromotionResponse toPromotionResponse(Promotion promotion);

    @Mapping(target = "promotionId", ignore = true)
    void updatePromotion(@MappingTarget Promotion promotion, PromotionUpdateRequest request);
}
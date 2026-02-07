package sp26.group3.computer.sba301_computershop.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeNameRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.entity.Attribute;
import sp26.group3.computer.sba301_computershop.entity.CategoriesAttributes;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeMappter {
    AttributeDetailResponse toAttributeDetailResponse(Attribute attribute);

    List<AttributeDetailResponse> toAttributeDetailResponseList(List<Attribute> attributes);

    Attribute toAttribute(AttributeNameRequest attributeNameRequest);

}

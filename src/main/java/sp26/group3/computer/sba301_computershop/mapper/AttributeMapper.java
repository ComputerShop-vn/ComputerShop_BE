package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.entity.Attribute;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

    @Mapping(target = "attributeId", ignore = true)
    Attribute toAttribute(AttributeCreationRequest request);

    AttributeResponse toAttributeResponse(Attribute attribute);

    @Mapping(target = "attributeId", ignore = true)
    void updateAttribute(@MappingTarget Attribute attribute, AttributeUpdateRequest request);
}
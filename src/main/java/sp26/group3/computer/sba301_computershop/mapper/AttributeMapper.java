package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.entity.Attribute;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

    Attribute toAttribute(AttributeCreationRequest request);

    AttributeResponse toAttributeResponse(Attribute attribute);

    void updateAttribute(@MappingTarget Attribute attribute, AttributeUpdateRequest request);
}
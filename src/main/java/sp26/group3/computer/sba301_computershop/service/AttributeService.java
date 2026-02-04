package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.AttributeCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;

import java.util.List;

public interface AttributeService {

    AttributeResponse createAttribute(AttributeCreationRequest request);

    AttributeResponse updateAttribute(int attributeId, AttributeUpdateRequest request);

    AttributeResponse getAttributeById(int attributeId);

    List<AttributeResponse> getAllAttributes();

    void deleteAttribute(int attributeId);
}
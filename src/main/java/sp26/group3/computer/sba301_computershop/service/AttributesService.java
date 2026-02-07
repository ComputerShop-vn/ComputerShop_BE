package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.AttributeNameRequest;
import sp26.group3.computer.sba301_computershop.dto.request.CategoryAttributeLinkRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;

import java.util.List;

public interface AttributesService {
    AttributeResponse addAttributesToCategory(CategoryAttributeLinkRequest req);

    List<AttributeDetailResponse> createAttributes(List<AttributeNameRequest> req);

    List<AttributeDetailResponse> getAllAttributes();

    AttributeDetailResponse getAttributeById(int id);

    AttributeDetailResponse updateAttribute(int id, AttributeNameRequest req);

    void deleteAttribute(int id);
}

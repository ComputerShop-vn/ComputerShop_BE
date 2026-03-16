package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;

import java.util.List;

public interface AttributeService {

    AttributeResponse createAttribute(AttributeCreationRequest request);

    AttributeResponse updateAttribute(int attributeId, AttributeUpdateRequest request);

    AttributeResponse getAttributeById(int attributeId);

    List<AttributeResponse> getAllAttributes();

    PagedResponse<AttributeResponse> getAllAttributesPaged(Pageable pageable);

    void deleteAttribute(int attributeId);
}
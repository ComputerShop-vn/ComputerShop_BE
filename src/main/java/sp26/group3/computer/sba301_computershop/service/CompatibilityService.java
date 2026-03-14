package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.CompatibleVariantsRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CompatibleVariantsResponse;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;

import java.util.List;

public interface CompatibilityService {

    CompatibleVariantsResponse getFilterHints(List<CompatibleVariantsRequest.ItemHint> items, ComponentType targetType);
}

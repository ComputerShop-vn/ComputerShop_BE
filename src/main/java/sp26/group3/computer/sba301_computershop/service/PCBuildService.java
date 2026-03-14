package sp26.group3.computer.sba301_computershop.service;

import jakarta.servlet.http.HttpServletRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AddBuildItemRequest;
import sp26.group3.computer.sba301_computershop.dto.request.CompatibleVariantsRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.SaveBuildNameRequest;
import sp26.group3.computer.sba301_computershop.dto.response.*;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;

import java.util.List;

public interface PCBuildService {

    PCBuildResponse getDraft();

    List<PCBuildResponse> getMyBuilds();

    PCBuildResponse upsertItem(AddBuildItemRequest request);

    PCBuildResponse saveBuild(SaveBuildNameRequest request);

    OrderResponse orderFromBuild(PlaceOrderRequest request, HttpServletRequest httpRequest);

    CompatibleVariantsResponse getCompatibleVariants(
            List<CompatibleVariantsRequest.ItemHint> items, ComponentType targetType);
}

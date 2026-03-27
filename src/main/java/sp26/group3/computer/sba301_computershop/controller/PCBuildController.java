package sp26.group3.computer.sba301_computershop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.AddBuildItemRequest;
import sp26.group3.computer.sba301_computershop.dto.request.CompatibleVariantsRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.SaveBuildNameRequest;
import sp26.group3.computer.sba301_computershop.dto.response.*;
import java.util.List;
import sp26.group3.computer.sba301_computershop.service.PCBuildService;


@RestController
@RequestMapping("/pc-builds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
public class PCBuildController {

    PCBuildService pcBuildService;

    @GetMapping("/draft")
    ApiResponse<PCBuildResponse> getDraft() {
        log.info("[GET] /pc-builds/draft");
        ApiResponse<PCBuildResponse> response = new ApiResponse<>();
        response.setResult(pcBuildService.getDraft());
        return response;
    }

    @GetMapping
    ApiResponse<List<PCBuildResponse>> getMyBuilds() {
        log.info("[GET] /pc-builds");
        ApiResponse<List<PCBuildResponse>> response = new ApiResponse<>();
        response.setResult(pcBuildService.getMyBuilds());
        return response;
    }

    @PostMapping("/compatible-variants")
    ApiResponse<CompatibleVariantsResponse> getCompatibleVariants(
            @RequestBody @Valid CompatibleVariantsRequest request) {
        log.info("[POST] /pc-builds/compatible-variants | targetType={}", request.getTargetComponentType());
        ApiResponse<CompatibleVariantsResponse> response = new ApiResponse<>();
        response.setResult(pcBuildService.getCompatibleVariants(
                request.getCurrentItems(), request.getTargetComponentType()));
        return response;
    }

    @PutMapping("/draft/items")
    ApiResponse<PCBuildResponse> upsertItem(
            @RequestBody @Valid AddBuildItemRequest request) {
        log.info("[PUT] /pc-builds/draft/items | componentType={} variantId={}",
                request.getComponentType(), request.getVariantId());
        ApiResponse<PCBuildResponse> response = new ApiResponse<>();
        response.setResult(pcBuildService.upsertItem(request));
        return response;
    }

    @DeleteMapping("/draft/items/{buildItemId}")
    ApiResponse<PCBuildResponse> removeItem(@PathVariable int buildItemId) {
        log.info("[DELETE] /pc-builds/draft/items/{}", buildItemId);
        ApiResponse<PCBuildResponse> response = new ApiResponse<>();
        response.setResult(pcBuildService.removeItem(buildItemId));
        return response;
    }

    @PutMapping("/draft/save")
    ApiResponse<PCBuildResponse> saveBuild(
            @RequestBody @Valid SaveBuildNameRequest request) {
        log.info("[PUT] /pc-builds/draft/save | name='{}'", request.getBuildName());
        ApiResponse<PCBuildResponse> response = new ApiResponse<>();
        response.setResult(pcBuildService.saveBuild(request));
        return response;
    }

    @PostMapping("/draft/order")
    ApiResponse<OrderResponse> orderFromBuild(
            @RequestBody @Valid PlaceOrderRequest request,
            HttpServletRequest httpRequest) {
        log.info("[POST] /pc-builds/draft/order");
        ApiResponse<OrderResponse> response = new ApiResponse<>();
        response.setResult(pcBuildService.orderFromBuild(request, httpRequest));
        return response;
    }
}

package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeNameRequest;
import sp26.group3.computer.sba301_computershop.dto.request.CategoryAttributeLinkRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.service.AttributesService;

import java.util.List;

@RestController
@RequestMapping("/attributes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttributesController {

    AttributesService attributesService;

    // ================= CREATE (Batch Loop) =================
    // Supports 1 or more creations
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AttributeDetailResponse>> createAttributes(
            @RequestBody @Valid List<AttributeNameRequest> request) {

        log.info("[POST] /attributes - Create attributes | count={}", request.size());

        List<AttributeDetailResponse> result = attributesService.createAttributes(request);

        return ApiResponse.<List<AttributeDetailResponse>>builder()
                .result(result)
                .message("Attributes created successfully")
                .build();
    }

    // ================= CREATE (Link to Category) =================
    @PostMapping("/link-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AttributeResponse> linkAttributesToCategory(
            @RequestBody @Valid CategoryAttributeLinkRequest request) {

        log.info("[POST] /attributes/link-category - Link attributes to category | categoryId={}, attrCount={}",
                request.getCategoryId(), request.getAttributeIds().size());

        AttributeResponse result = attributesService.addAttributesToCategory(request);

        return ApiResponse.<AttributeResponse>builder()
                .result(result)
                .message("Attributes linked to category successfully")
                .build();
    }

    // ================= READ =================
    @GetMapping
    public ApiResponse<List<AttributeDetailResponse>> getAllAttributes() {
        log.info("[GET] /attributes - Get all attributes");

        List<AttributeDetailResponse> result = attributesService.getAllAttributes();

        return ApiResponse.<List<AttributeDetailResponse>>builder()
                .result(result)
                .message("Get all attributes successfully")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AttributeDetailResponse> getAttributeById(@PathVariable int id) {
        log.info("[GET] /attributes/{} - Get attribute by id", id);

        AttributeDetailResponse result = attributesService.getAttributeById(id);

        return ApiResponse.<AttributeDetailResponse>builder()
                .result(result)
                .message("Get attribute details successfully")
                .build();
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AttributeDetailResponse> updateAttribute(
            @PathVariable int id,
            @RequestBody @Valid AttributeNameRequest request) {

        log.info("[PUT] /attributes/{} - Update attribute", id);

        AttributeDetailResponse result = attributesService.updateAttribute(id, request);

        return ApiResponse.<AttributeDetailResponse>builder()
                .result(result)
                .message("Attribute updated successfully")
                .build();
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAttribute(@PathVariable int id) {
        log.info("[DELETE] /attributes/{} - Delete attribute", id);

        attributesService.deleteAttribute(id);

        return ApiResponse.<Void>builder()
                .message("Attribute deleted successfully")
                .build();
    }
}
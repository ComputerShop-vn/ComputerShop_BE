package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.service.AttributeService;

import java.util.List;

@RestController
@RequestMapping("/attributes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttributeController {

    AttributeService attributeService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<AttributeResponse> createAttribute(@RequestBody @Valid AttributeCreationRequest request) {
        log.info("[POST] /attributes - Create attribute");

        AttributeResponse result = attributeService.createAttribute(request);

        log.info("[POST] /attributes - SUCCESS | attributeId={}", result.getAttributeId());

        ApiResponse<AttributeResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ ALL =================
    @GetMapping
    public ApiResponse<List<AttributeResponse>> getAllAttributes() {
        log.info("[GET] /attributes - Get all attributes");

        List<AttributeResponse> result = attributeService.getAllAttributes();

        log.info("[GET] /attributes - Total attributes={}", result.size());

        ApiResponse<List<AttributeResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    public ApiResponse<AttributeResponse> getAttributeById(@PathVariable int id) {
        log.info("[GET] /attributes/{} - Get attribute by id", id);

        AttributeResponse result = attributeService.getAttributeById(id);

        log.info("[GET] /attributes/{} - SUCCESS | attributeName={}", id, result.getAttributeName());

        ApiResponse<AttributeResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<AttributeResponse> updateAttribute(
            @PathVariable int id,
            @RequestBody @Valid AttributeUpdateRequest request
    ) {
        log.info("[PUT] /attributes/{} - Update attribute", id);

        AttributeResponse result = attributeService.updateAttribute(id, request);

        log.info("[PUT] /attributes/{} - Update SUCCESS", id);

        ApiResponse<AttributeResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deleteAttribute(@PathVariable int id) {
        log.warn("[DELETE] /attributes/{} - Delete attribute", id);

        attributeService.deleteAttribute(id);

        log.warn("[DELETE] /attributes/{} - SUCCESS", id);

        return new ApiResponse<>();
    }
}
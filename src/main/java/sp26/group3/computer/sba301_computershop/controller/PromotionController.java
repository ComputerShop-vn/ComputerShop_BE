package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PromotionResponse;
import sp26.group3.computer.sba301_computershop.service.PromotionService;

import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionController {

    PromotionService promotionService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<PromotionResponse> createPromotion(@RequestBody @Valid PromotionCreationRequest request) {
        log.info("[POST] /promotions - Create promotion");

        PromotionResponse result = promotionService.createPromotion(request);

        log.info("[POST] /promotions - SUCCESS | promotionId={}", result.getPromotionId());

        ApiResponse<PromotionResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ ALL =================
    @GetMapping
    public ApiResponse<List<PromotionResponse>> getAllPromotions() {
        log.info("[GET] /promotions - Get all promotions");

        List<PromotionResponse> result = promotionService.getAllPromotions();

        log.info("[GET] /promotions - Total promotions={}", result.size());

        ApiResponse<List<PromotionResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    public ApiResponse<PromotionResponse> getPromotionById(@PathVariable int id) {
        log.info("[GET] /promotions/{} - Get promotion by id", id);

        PromotionResponse result = promotionService.getPromotionById(id);

        log.info("[GET] /promotions/{} - SUCCESS | promoCode={}", id, result.getPromoCode());

        ApiResponse<PromotionResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ BY CODE =================
    @GetMapping("/code/{promoCode}")
    public ApiResponse<PromotionResponse> getPromotionByCode(@PathVariable String promoCode) {
        log.info("[GET] /promotions/code/{} - Get promotion by code", promoCode);

        PromotionResponse result = promotionService.getPromotionByCode(promoCode);

        log.info("[GET] /promotions/code/{} - SUCCESS", promoCode);

        ApiResponse<PromotionResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<PromotionResponse> updatePromotion(
            @PathVariable int id,
            @RequestBody @Valid PromotionUpdateRequest request
    ) {
        log.info("[PUT] /promotions/{} - Update promotion", id);

        PromotionResponse result = promotionService.updatePromotion(id, request);

        log.info("[PUT] /promotions/{} - Update SUCCESS", id);

        ApiResponse<PromotionResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deletePromotion(@PathVariable int id) {
        log.warn("[DELETE] /promotions/{} - Delete promotion", id);

        promotionService.deletePromotion(id);

        log.warn("[DELETE] /promotions/{} - SUCCESS", id);

        return new ApiResponse<>();
    }
}
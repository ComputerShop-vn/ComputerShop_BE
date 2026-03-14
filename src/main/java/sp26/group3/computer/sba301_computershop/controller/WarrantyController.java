package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateWarrantyStatusRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.WarrantyResponse;
import sp26.group3.computer.sba301_computershop.service.WarrantyService;

import java.util.List;

@RestController
@RequestMapping("/warranties")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarrantyController {

    WarrantyService warrantyService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<WarrantyResponse> getWarrantyById(@PathVariable int id) {
        WarrantyResponse response = warrantyService.getWarrantyById(id);
        ApiResponse<WarrantyResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<List<WarrantyResponse>> getWarrantiesByOrderId(@PathVariable int orderId) {
        List<WarrantyResponse> response = warrantyService.getWarrantiesByOrderId(orderId);
        ApiResponse<List<WarrantyResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<List<WarrantyResponse>> getWarrantiesByPhoneNumber(@PathVariable String phoneNumber) {
        List<WarrantyResponse> response = warrantyService.getWarrantiesByPhoneNumber(phoneNumber);
        ApiResponse<List<WarrantyResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<WarrantyResponse> updateWarrantyStatus(
            @PathVariable int id,
            @RequestBody @Valid UpdateWarrantyStatusRequest request) {
        WarrantyResponse response = warrantyService.updateWarrantyStatus(id, request);
        ApiResponse<WarrantyResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }
}

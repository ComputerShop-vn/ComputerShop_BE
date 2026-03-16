package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.ResetPasswordRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.service.ForgotPasswordService;

import java.util.Map;

@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ForgotPasswordController {

    ForgotPasswordService forgotPasswordService;

    /**
     * Đặt lại mật khẩu
     * Flow: send-otp → verify-otp → reset/{email}
     */
    @PostMapping("/reset/{email}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(
            @PathVariable String email,
            @Valid @RequestBody ResetPasswordRequest request) {

        // Gọi service xử lý toàn bộ logic
        forgotPasswordService.resetPassword(email, request);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .code(1000)
                        .message("Đặt lại mật khẩu thành công!")
                        .result(Map.of("success", true))
                        .build()
        );
    }
}
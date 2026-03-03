package sp26.group3.computer.sba301_computershop.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;

import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PaymentDTO;
import sp26.group3.computer.sba301_computershop.service.PaymentService;

@RestController
@RequestMapping("/orders/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @GetMapping("/createPayment")
    public ApiResponse<PaymentDTO> createPayment(
            @RequestParam int orderId,
            @RequestParam(required = false) String bankCode,
            HttpServletRequest request) {

        ApiResponse<PaymentDTO> response = new ApiResponse<>();
        response.setResult(paymentService.createVnPayPayment(request, orderId, bankCode));
        return response;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> paymentCallback(HttpServletRequest request) {
        String redirectUrl = paymentService.handleVnPayCallback(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/vnp-ipn")
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        return paymentService.handleVnPayIpn(request);
    }
}

package sp26.group3.computer.sba301_computershop.service;

import jakarta.servlet.http.HttpServletRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PaymentDTO;

import java.util.Map;

public interface PaymentService {
    PaymentDTO createVnPayPayment(HttpServletRequest request, int orderId, String bankCode);

    String handleVnPayCallback(HttpServletRequest request);

    //void handleVnPayIpn(HttpServletRequest request);
    Map<String, String> handleVnPayIpn(HttpServletRequest request);
}

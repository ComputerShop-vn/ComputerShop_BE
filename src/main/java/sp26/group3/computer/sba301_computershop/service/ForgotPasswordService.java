package sp26.group3.computer.sba301_computershop.service;


import sp26.group3.computer.sba301_computershop.dto.request.ResetPasswordRequest;

public interface ForgotPasswordService {
    void resetPassword(String email, ResetPasswordRequest request);
}
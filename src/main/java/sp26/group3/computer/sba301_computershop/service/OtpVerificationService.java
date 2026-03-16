package sp26.group3.computer.sba301_computershop.service;


public interface OtpVerificationService {
    public String sendOtp(String email) ;
    public String verifyOtp(Integer otp, String email);
    void deleteOtpByEmail(String email);
}
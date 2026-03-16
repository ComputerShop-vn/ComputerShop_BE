package sp26.group3.computer.sba301_computershop.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.MailBody;
import sp26.group3.computer.sba301_computershop.entity.OtpVerification;
import sp26.group3.computer.sba301_computershop.repository.OtpVerificationRepository;
import sp26.group3.computer.sba301_computershop.service.OtpVerificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpVerificationServiceImpl implements OtpVerificationService {

    JavaMailSender javaMailSender;
    OtpVerificationRepository otpVerificationRepository;

    public String sendOtp(String email) {
        // Tạo OTP
        int otp = otpGenerator();
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + 2 * 60 * 1000);

        // Lưu OTP vào database
        Optional<OtpVerification> existingOtp = otpVerificationRepository.findByEmail(email);
        OtpVerification otpVerification;

        if (existingOtp.isPresent()) {
            otpVerification = existingOtp.get();
            otpVerification.setOtp(otp);
            otpVerification.setCreatedAt(now);
            otpVerification.setExpirationTime(expirationTime);
        } else {
            otpVerification = OtpVerification.builder()
                    .email(email)
                    .otp(otp)
                    .expirationTime(expirationTime)
                    .createdAt(now)
                    .build();
        }

        otpVerificationRepository.save(otpVerification);

        // Đọc HTML template & replace OTP
        String html;
        try {
            String templatePath = "src/main/resources/templates/otp_template.html";
            String htmlTemplate = Files.readString(Paths.get(templatePath));
            html = htmlTemplate.replace("${otp}", String.valueOf(otp));
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được file template OTP email", e);
        }

        // Gửi email HTML
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("OTP cho xác thực tài khoản")
                .text(html)
                .build();
        sendSimpleMessage(mailBody);

        return "OTP đã được gửi đến email!";
    }

    public String verifyOtp(Integer otp, String email) {
        OtpVerification otpVerification = otpVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy OTP cho email: " + email));

        if (otpVerification.getExpirationTime().before(new Date())) {
            throw new RuntimeException("OTP đã hết hạn.");
        }

        if (!otpVerification.getOtp().equals(otp)) {
            throw new RuntimeException("OTP không đúng.");
        }

        return "OTP xác thực thành công!";
    }

    private void sendSimpleMessage(MailBody mailBody) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(mailBody.to());
            helper.setFrom("vinhhien8882004@gmail.com");
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.text(), true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }

    @Override
    @Transactional
    public void deleteOtpByEmail(String email) {
        otpVerificationRepository.deleteByEmail(email);
    }

    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
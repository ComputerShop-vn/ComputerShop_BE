package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.service.EmailService;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendNearDueReminder(String email, String customerName, String installmentNo, double amount,
            String dueDate) {
        String subject = "[Thông báo] Nhắc nhở thanh toán trả góp kỳ #" + installmentNo;
        String content = String.format(
                "<h3>Xin chào %s,</h3>" +
                        "<p>Đây là email nhắc nhở khoản thanh toán trả góp kỳ <b>#%s</b> của bạn với số tiền <b>%,.0f VNĐ</b> sẽ đến hạn vào ngày <b>%s</b>.</p>"
                        +
                        "<p>Vui lòng sắp xếp thanh toán đúng hạn để tránh phát sinh thêm các khoản phí phạt trễ hạn.</p>"
                        +
                        "<p>Trân trọng,<br/>Đội ngũ vitinh.com</p>",
                customerName, installmentNo, amount, dueDate);
        sendEmail(email, subject, content);
    }

    @Override
    public void sendOverdueNotification(String email, String customerName, String installmentNo, double amount,
            String dueDate) {
        String subject = "[QUAN TRỌNG] Thông báo QUÁ HẠN thanh toán trả góp kỳ #" + installmentNo;
        String content = String.format(
                "<h3>Xin chào %s,</h3>" +
                        "<p>Khoản thanh toán trả góp kỳ <b>#%s</b> của bạn với số tiền <b>%,.0f VNĐ</b> (hạn chót ngày <b>%s</b>) hiện đã <b>QUÁ HẠN</b>.</p>"
                        +
                        "<p>Hệ thống đang áp dụng phí phạt trễ hạn cộng dồn mỗi ngày lên tài khoản của bạn. Vui lòng thanh toán NGAY LẬP TỨC để tránh phát sinh thêm chi phí phạt và bị khóa tài khoản mua hàng.</p>"
                        +
                        "<p>Trân trọng,<br/>Đội ngũ vitinh.com</p>",
                customerName, installmentNo, amount, dueDate);
        sendEmail(email, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

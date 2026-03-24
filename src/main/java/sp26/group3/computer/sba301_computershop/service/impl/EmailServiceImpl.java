package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendNearDueReminder(String email, String customerName, String orderId, String installmentNo,
            double amount, LocalDate dueDate) {
        String subject = "[Thông báo] Nhắc nhở thanh toán trả góp kỳ #" + installmentNo;
        String title = "Nhắc nhở thanh toán (Sắp đến hạn)";
        String message = String.format(
                "Đây là email nhắc nhở khoản thanh toán trả góp kỳ <b>#%s</b> của bạn sắp đến hạn.",
                installmentNo);

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("customerName", customerName);
        context.setVariable("orderId", orderId);
        context.setVariable("message", message);
        context.setVariable("installmentNo", "#" + installmentNo);
        context.setVariable("amount", amount);
        context.setVariable("dueDate", dueDate);

        String processContent = templateEngine.process("email-template", context);
        sendEmail(email, subject, processContent);
    }

    @Override
    public void sendOverdueNotification(String email, String customerName, String orderId, String installmentNo,
            double amount, LocalDate dueDate) {
        String subject = "[QUAN TRỌNG] Thông báo QUÁ HẠN thanh toán trả góp kỳ #" + installmentNo;
        String title = "CẢNH BÁO QUÁ HẠN THANH TOÁN";
        String message = String.format(
                "Khoản thanh toán trả góp kỳ <b>#%s</b> của bạn đã <b>QUÁ HẠN</b>.<br/>" +
                "Hệ thống đang áp dụng phí phạt trễ hạn cộng dồn mỗi ngày lên tài khoản của bạn. " +
                "Vui lòng thanh toán NGAY LẬP TỨC để tránh phát sinh thêm chi phí phạt.",
                installmentNo);

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("customerName", customerName);
        context.setVariable("orderId", orderId);
        context.setVariable("message", message);
        context.setVariable("installmentNo", "#" + installmentNo);
        context.setVariable("amount", amount);
        context.setVariable("dueDate", dueDate);

        String processContent = templateEngine.process("email-template", context);
        sendEmail(email, subject, processContent);
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

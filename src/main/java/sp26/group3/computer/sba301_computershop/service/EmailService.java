package sp26.group3.computer.sba301_computershop.service;

import java.time.LocalDate;

public interface EmailService {
    void sendNearDueReminder(String email, String customerName, String orderId, String installmentNo, double amount, LocalDate dueDate);
    void sendOverdueNotification(String email, String customerName, String orderId, String installmentNo, double amount, LocalDate dueDate);
}

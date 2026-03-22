package sp26.group3.computer.sba301_computershop.service;

public interface EmailService {
    void sendNearDueReminder(String email, String customerName, String installmentNo, double amount, String dueDate);
    void sendOverdueNotification(String email, String customerName, String installmentNo, double amount, String dueDate);
}

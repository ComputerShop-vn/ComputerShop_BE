package sp26.group3.computer.sba301_computershop.service;

public interface PaymentScheduleJobService {
    void runDailyPaymentJobs();
    void handleNearDueReminders();
    void handleMarkAsOverdue();
    void handleCalculateDailyPenalty();
}

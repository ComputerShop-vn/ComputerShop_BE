package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.entity.OrderPaymentSchedule;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.repository.OrderPaymentScheduleRepository;
import sp26.group3.computer.sba301_computershop.service.EmailService;
import sp26.group3.computer.sba301_computershop.service.PaymentScheduleJobService;
import sp26.group3.computer.sba301_computershop.service.PenaltyCalculationService;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentScheduleJobServiceImpl implements PaymentScheduleJobService {

    private final OrderPaymentScheduleRepository scheduleRepository;
    private final EmailService emailService;
    private final PenaltyCalculationService penaltyCalculationService;

    @Override
    @Scheduled(cron = "0 1 0 * * ?")
    public void runDailyPaymentJobs() {
        log.info("Starting daily payment schedule jobs...");
        handleNearDueReminders();
        handleMarkAsOverdue();
        handleCalculateDailyPenalty();
        log.info("Finished daily payment schedule jobs.");
    }

    @Override
    @Transactional
    public void handleNearDueReminders() {
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        List<OrderPaymentSchedule> nearDueSchedules = scheduleRepository
                .findByStatusAndDueDate(PaymentStatus.UNPAID, threeDaysFromNow);

        log.info("Task 1: Sending near-due reminders for {} records.", nearDueSchedules.size());
        
        for (OrderPaymentSchedule schedule : nearDueSchedules) {
            try {
                emailService.sendNearDueReminder(
                        schedule.getOrder().getUser().getEmail(),
                        schedule.getOrder().getUser().getUsername(),
                        String.valueOf(schedule.getInstallmentNo()),
                        schedule.getAmount(),
                        schedule.getDueDate().toString()
                );
            } catch (Exception e) {
                log.error("Failed to send near-due reminder for schedule ID {}: {}", schedule.getPaymentScheduleId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void handleMarkAsOverdue() {
        LocalDate today = LocalDate.now();
        List<OrderPaymentSchedule> overdueSchedules = scheduleRepository
                .findByStatusAndDueDateBefore(PaymentStatus.UNPAID, today);

        log.info("Task 2: Marking {} records as OVERDUE.", overdueSchedules.size());

        for (OrderPaymentSchedule schedule : overdueSchedules) {
            try {
                schedule.setStatus(PaymentStatus.OVERDUE);
                scheduleRepository.save(schedule);

                emailService.sendOverdueNotification(
                        schedule.getOrder().getUser().getEmail(),
                        schedule.getOrder().getUser().getUsername(),
                        String.valueOf(schedule.getInstallmentNo()),
                        schedule.getAmount(),
                        schedule.getDueDate().toString()
                );
            } catch (Exception e) {
                log.error("Failed to mark schedule ID {} as overdue: {}", schedule.getPaymentScheduleId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void handleCalculateDailyPenalty() {
        List<OrderPaymentSchedule> alreadyOverdue = scheduleRepository
                .findByStatus(PaymentStatus.OVERDUE);

        log.info("Task 3: Calculating daily penalty for {} records.", alreadyOverdue.size());

        for (OrderPaymentSchedule schedule : alreadyOverdue) {
            try {
                if (schedule.getOrder().getInstallmentPackage() != null) {
                    double annualRate = schedule.getOrder().getInstallmentPackage().getAnnualPenaltyRate();
                    double dailyPenalty = penaltyCalculationService.calculateDailyPenalty(schedule.getAmount(), annualRate);
                    
                    double newPenaltyTotal = schedule.getPenaltyAmount() + dailyPenalty;
                    schedule.setPenaltyAmount(newPenaltyTotal);
                    
                    scheduleRepository.save(schedule);
                }
            } catch (Exception e) {
                log.error("Failed to calculate penalty for schedule ID {}: {}", schedule.getPaymentScheduleId(), e.getMessage());
            }
        }
    }
}

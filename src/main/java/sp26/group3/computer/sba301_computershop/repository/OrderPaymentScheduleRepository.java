package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.OrderPaymentSchedule;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderPaymentScheduleRepository extends JpaRepository<OrderPaymentSchedule, Integer> {
    List<OrderPaymentSchedule> findByOrderOrderIdOrderByInstallmentNoAsc(int orderId);

    // Task 1: Find records UNPAID and due exactly in 3 days
    List<OrderPaymentSchedule> findByStatusAndDueDate(PaymentStatus status, LocalDate dueDate);

    // Task 2: Find records UNPAID and overdue (dueDate < current date)
    List<OrderPaymentSchedule> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate currentDate);

    // Task 3: Find records that are already OVERDUE
    List<OrderPaymentSchedule> findByStatus(PaymentStatus status);
}

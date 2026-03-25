package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sp26.group3.computer.sba301_computershop.entity.OrderPaymentSchedule;
import sp26.group3.computer.sba301_computershop.repository.projection.InstallmentOrderProjection;
import sp26.group3.computer.sba301_computershop.repository.projection.InstallmentSummaryProjection;
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

    @Query(value = "SELECT " +
                   "SUM(CASE WHEN ops.status = 'PAID' THEN ops.amount ELSE 0 END) as totalPaid, " +
                   "SUM(CASE WHEN ops.status = 'UNPAID' THEN ops.amount ELSE 0 END) as totalUnpaid, " +
                   "SUM(CASE WHEN ops.status = 'OVERDUE' THEN ops.amount ELSE 0 END) as totalOverdue " +
                   "FROM order_payment_schedule ops " +
                   "JOIN orders o ON ops.order_id = o.order_id " +
                   "WHERE o.payment_type = 'INSTALLMENT'",
           nativeQuery = true)
    InstallmentSummaryProjection getInstallmentSummary();

    @Query(value = "SELECT o.order_id as orderId, u.username as customerUsername, " +
                   "o.total_amount as orderTotal, " +
                   "COUNT(ops.payment_schedule_id) as totalInstallments, " +
                   "SUM(CASE WHEN ops.status = 'PAID' THEN 1 ELSE 0 END) as paidInstallments, " +
                   "CONVERT(NVARCHAR, MIN(CASE WHEN ops.status IN ('UNPAID', 'OVERDUE') THEN ops.due_date ELSE NULL END), 23) as nextDueDate " +
                   "FROM orders o " +
                   "JOIN order_payment_schedule ops ON o.order_id = ops.order_id " +
                   "JOIN users u ON o.user_id = u.user_id " +
                   "WHERE o.payment_type = 'INSTALLMENT' " +
                   "GROUP BY o.order_id, u.username, o.total_amount " +
                   "ORDER BY o.order_id DESC",
           nativeQuery = true)
    List<InstallmentOrderProjection> findInstallmentOrderDetails();
}

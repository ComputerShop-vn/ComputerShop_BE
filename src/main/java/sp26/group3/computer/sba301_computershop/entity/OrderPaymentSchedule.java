package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentType;

import java.time.LocalDate;

@Entity
@Table(name = "order_payment_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_schedule_id")
    private int paymentScheduleId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "duration_months")
    private int durationMonths;

    @Column(name = "interest_rate")
    private double interestRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType; // FULL, INSTALLMENT

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "installment_no")
    private int installmentNo;

    private double amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, PAID, OVERDUE
}


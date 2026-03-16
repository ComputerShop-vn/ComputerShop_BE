package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "installment_package")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstallmentPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    int packageId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "duration_months", nullable = false)
    int durationMonths;

    @Column(name = "interest_rate")
    double interestRate;

    @Column(name = "min_order_amount")
    double minOrderAmount;

    @Column(name = "down_payment_percentage")
    double downPaymentPercentage;

    @Column(name = "is_active")
    boolean isActive;
}

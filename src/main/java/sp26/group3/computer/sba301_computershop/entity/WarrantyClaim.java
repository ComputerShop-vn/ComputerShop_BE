package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.ClaimStatus;
import sp26.group3.computer.sba301_computershop.enums.SolutionType;

import java.time.LocalDate;

@Entity
@Table(name = "warranty_claim")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private int claimId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id", nullable = false)
    private Warranty warranty;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "customer_note", columnDefinition = "NVARCHAR(MAX)")
    private String customerNote;

    @Column(name = "technician_note", columnDefinition = "NVARCHAR(MAX)")
    private String technicianNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "solution_type")
    private SolutionType solutionType;

    @Column(name = "return_date")
    private LocalDate returnDate;
}

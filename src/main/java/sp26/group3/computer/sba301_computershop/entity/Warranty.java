package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import sp26.group3.computer.sba301_computershop.enums.WarrantyStatus;
import sp26.group3.computer.sba301_computershop.enums.WarrantyType;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "warranties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @OneToMany(mappedBy = "warranty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WarrantyClaim> claims;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyType type;
}

package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.BuildStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "pc_build")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PCBuild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "build_id")
    private int buildId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "build_name")
    private String buildName;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private BuildStatus status; // DRAFT, SAVED, ORDERED
}


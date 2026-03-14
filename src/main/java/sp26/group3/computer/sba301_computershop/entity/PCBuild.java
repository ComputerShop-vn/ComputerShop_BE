package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.BuildStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pc_builds")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BuildStatus status; // DRAFT, SAVED, ORDERED

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PCBuildItem> items = new ArrayList<>();
}


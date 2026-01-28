package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pc_build_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PCBuildItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "build_item_id")
    private int buildItemId;

    @ManyToOne
    @JoinColumn(name = "build_id", nullable = false)
    private PCBuild build;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;
}


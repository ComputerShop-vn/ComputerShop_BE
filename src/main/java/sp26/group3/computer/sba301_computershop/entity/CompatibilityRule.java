package sp26.group3.computer.sba301_computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.RuleType;

@Entity
@Table(name = "compatibility_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private int ruleId;

    // Loại linh kiện thứ nhất (bắt buộc)
    @Column(name = "component_type_1", nullable = false, length = 50)
    private String componentType1;

    // Loại linh kiện thứ hai (null đối với MIN_WATTAGE)
    @Column(name = "component_type_2", length = 50)
    private String componentType2;

    // Tên attribute cần so sánh trên component 1 (VD: "Socket", "Memory Type", "Wattage")
    @Column(name = "attribute_1", nullable = false, length = 100)
    private String attribute1;

    // Tên attribute cần so sánh trên component 2 (null đối với MIN_WATTAGE)
    @Column(name = "attribute_2", length = 100)
    private String attribute2;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 20)
    private RuleType ruleType;

    @Column(length = 500)
    private String description;
}

package sp26.group3.computer.sba301_computershop.enums;

public enum RuleType {
    /**
     * attribute_1 của component_type_1 phải bằng attribute_2 của component_type_2.
     * VD: CPU.Socket == MAINBOARD.Socket
     */
    MUST_MATCH,

    /**
     * Giá trị numeric của attribute_1 phải <= attribute_2.
     * VD: RAM.Speed <= MAINBOARD.MaxRamSpeed
     */
    MUST_SUPPORT,

    /**
     * Giá trị numeric của attribute_1 phải <= attribute_2 (về kích thước).
     * VD: GPU.Length <= CASE.MaxGpuLength
     */
    MUST_FIT,

    /**
     * Rule đặc biệt: PSU.Wattage phải đủ cấp điện cho cả hệ thống.
     * component_type_2 = null, attribute_1 = "Wattage"
     */
    MIN_WATTAGE
}

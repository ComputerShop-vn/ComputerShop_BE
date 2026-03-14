package sp26.group3.computer.sba301_computershop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComponentType {
    //                    displayName                      categoryName   multiSlot  required
    CPU              ("CPU / Bộ xử lý",                  "CPU",         false, true),
    MAINBOARD        ("Bo mạch chủ",                     "Mainboard",   false, true),
    RAM              ("RAM / Bộ nhớ",                    "RAM",         true,  true),
    GPU              ("Card đồ họa",                     "GPU",         false, false),
    STORAGE_PRIMARY  ("Ổ cứng chính (SSD/NVMe)",         "SSD",         false, true),
    STORAGE_SECONDARY("Ổ cứng phụ (HDD/SSD)",            "HDD",         true,  false),
    PSU              ("Nguồn máy tính",                  "PSU",         false, true),
    CASE             ("Vỏ case",                         "Case",        false, true),
    COOLING          ("Tản nhiệt",                       "Cooling",     true,  false),
    MONITOR          ("Màn hình",                        "Monitor",     true,  false),
    KEYBOARD         ("Bàn phím",                        "Keyboard",    true,  false),
    MOUSE            ("Chuột",                           "Mouse",       true,  false);

    private final String displayName;
    /** Tên category trong DB (dùng để lookup categoryId khi trả về filter hints) */
    private final String categoryName;
    /** true = cho phép nhiều slot (RAM, Ổ phụ, Quạt...) */
    private final boolean multiSlot;
    /** true = bắt buộc phải có trong build hoàn chỉnh */
    private final boolean required;
}

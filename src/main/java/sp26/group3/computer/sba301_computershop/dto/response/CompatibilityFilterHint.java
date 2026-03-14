package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Dùng cho POST /pc-builds/compatible-variants
 * Mỗi hint là 1 ràng buộc attribute mà variant của componentType mục tiêu cần thỏa mãn.
 *
 * FE dùng list này để gọi product search API:
 *   - MUST_MATCH  (eq):  GET /products?attributes=Socket:AM5
 *   - MUST_FIT /
 *     MUST_SUPPORT (lte): GET /products?attributes=GPU Length:lte:336
 *   - MIN_WATTAGE (gte): GET /products?attributes=Wattage:gte:750
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompatibilityFilterHint {

    /** Tên attribute cần filter. VD: "Socket", "GPU Length", "Wattage" */
    String attributeName;

    /**
     * Giá trị ngưỡng dùng để so sánh.
     * Với EQ: giá trị exact. Với LTE/GTE: giá trị numeric.
     */
    String requiredValue;

    /** Loại rule gốc: MUST_MATCH | MUST_SUPPORT | MUST_FIT | MIN_WATTAGE */
    String ruleType;

    /**
     * Toán tử so sánh để FE tạo query:
     *   "eq"  → attributes={attr}:{value}
     *   "lte" → attributes={attr}:lte:{value}  (giá trị variant ≤ requiredValue)
     *   "gte" → attributes={attr}:gte:{value}  (giá trị variant ≥ requiredValue)
     */
    String comparison;
}

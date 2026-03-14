package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Response cho POST /pc-builds/compatible-variants
 *
 * - categoryId: FE dùng ngay để gọi GET /products?categoryId={categoryId}&attributes=...
 * - hints:      danh sách ràng buộc attribute (eq / lte / gte)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompatibleVariantsResponse {

    /**
     * categoryId trong DB tương ứng với targetComponentType.
     * Null nếu không tìm thấy category khớp (không nên xảy ra).
     */
    Integer categoryId;

    /** Danh sách filter hints để FE build query string */
    List<CompatibilityFilterHint> hints;
}

package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private int categoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;
}

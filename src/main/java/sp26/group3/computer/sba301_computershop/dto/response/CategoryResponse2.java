package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponse2 {

    Integer categoryId;
    String categoryName;
    Integer parentCategoryId;

    List<CategoryResponse2> children;
}

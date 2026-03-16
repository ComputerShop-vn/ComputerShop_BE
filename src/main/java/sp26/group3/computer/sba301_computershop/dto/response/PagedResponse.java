package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagedResponse<T> {
    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean last;
}

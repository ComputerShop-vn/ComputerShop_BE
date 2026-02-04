package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BlogResponse {
    private int blogId;
    private int userId;
    private String userName; // Để hiển thị tên user
    private String title;
    private String content;
    private LocalDateTime publishedAt;
}
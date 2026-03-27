package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private int senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    @JsonProperty("isRead")
    private boolean isRead;
}

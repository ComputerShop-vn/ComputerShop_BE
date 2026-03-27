package sp26.group3.computer.sba301_computershop.dto.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private int receiverId;
    private String content;
}

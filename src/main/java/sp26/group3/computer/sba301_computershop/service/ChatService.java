package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.ChatMessageRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ChatMessageResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ConversationResponse;

import java.util.List;

public interface ChatService {
    public List<ConversationResponse> getConversations();
    public List<ChatMessageResponse> getChatHistory(int otherUserId);
    public void markAsRead(int otherUserId);
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderEmail);
}

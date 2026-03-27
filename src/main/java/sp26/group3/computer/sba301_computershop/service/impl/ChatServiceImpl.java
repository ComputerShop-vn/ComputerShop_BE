package sp26.group3.computer.sba301_computershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.ChatMessageRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ChatMessageResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ConversationResponse;
import sp26.group3.computer.sba301_computershop.entity.ChatConversation;
import sp26.group3.computer.sba301_computershop.entity.ChatMessage;
import sp26.group3.computer.sba301_computershop.entity.User;
import sp26.group3.computer.sba301_computershop.repository.ChatConversationRepository;
import sp26.group3.computer.sba301_computershop.repository.ChatMessageRepository;
import sp26.group3.computer.sba301_computershop.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String buildRoomKey(int id1, int id2) {
        return Math.min(id1, id2) + "_" + Math.max(id1, id2);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations() {
        User me = getCurrentUser();
        return conversationRepo.findAllByUserId(me.getUserId()).stream()
                .map(conv -> {
                    // Eager access within the stream — safe because findAllByUserId fetches the full entity
                    int user1Id = conv.getUser1().getUserId();
                    User other = user1Id == me.getUserId() ? conv.getUser2() : conv.getUser1();
                    long unread = messageRepo.countUnread(conv.getId(), me.getUserId());
                    return ConversationResponse.builder()
                            .id(conv.getId())
                            .roomKey(conv.getRoomKey())
                            .otherUserId(other.getUserId())
                            .otherUserName(other.getUsername())
                            .otherUserRole(other.getRole().getName())
                            .lastMessage(conv.getLastMessage())
                            .lastMessageAt(conv.getLastMessageAt())
                            .unreadCount(unread)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(int otherUserId) {
        User me = getCurrentUser();
        String roomKey = buildRoomKey(me.getUserId(), otherUserId);
        return conversationRepo.findByRoomKey(roomKey)
                .map(conv -> {
                    Long convId = conv.getId();
                    return messageRepo.findByConversationIdOrderBySentAtAsc(convId).stream()
                            .map(msg -> toResponseWithConvId(msg, convId))
                            .collect(Collectors.toList());
                })
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void markAsRead(int otherUserId) {
        User me = getCurrentUser();
        String roomKey = buildRoomKey(me.getUserId(), otherUserId);
        conversationRepo.findByRoomKey(roomKey)
                .ifPresent(conv -> messageRepo.markAllAsRead(conv.getId(), me.getUserId()));
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderEmail) {
        User sender = userRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepo.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        String roomKey = buildRoomKey(sender.getUserId(), receiver.getUserId());

        ChatConversation conv = conversationRepo.findByRoomKey(roomKey)
                .orElseGet(() -> conversationRepo.save(
                        ChatConversation.builder()
                                .roomKey(roomKey)
                                .user1(sender.getUserId() < receiver.getUserId() ? sender : receiver)
                                .user2(sender.getUserId() < receiver.getUserId() ? receiver : sender)
                                .lastMessageAt(LocalDateTime.now())
                                .build()
                ));

        ChatMessage msg = messageRepo.save(ChatMessage.builder()
                .conversation(conv)
                .sender(sender)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build());

        conv.setLastMessage(request.getContent());
        conv.setLastMessageAt(msg.getSentAt());
        conversationRepo.save(conv);

        ChatMessageResponse response = toResponseWithConvId(msg, conv.getId());

        // Broadcast to WebSocket topic
        messagingTemplate.convertAndSend("/topic/chat." + roomKey, response);

        return response;
    }

    private ChatMessageResponse toResponseWithConvId(ChatMessage msg, Long convId) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .conversationId(convId)
                .senderId(msg.getSender().getUserId())
                .senderName(msg.getSender().getUsername())
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .isRead(msg.isRead())
                .build();
    }
}

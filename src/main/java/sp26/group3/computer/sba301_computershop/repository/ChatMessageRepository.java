package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group3.computer.sba301_computershop.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.sender s WHERE m.conversation.id = :conversationId ORDER BY m.sentAt ASC")
    List<ChatMessage> findByConversationIdOrderBySentAtAsc(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :convId AND m.sender.userId != :userId AND m.isRead = false")
    long countUnread(@Param("convId") Long convId, @Param("userId") int userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation.id = :convId AND m.sender.userId != :userId AND m.isRead = false")
    void markAllAsRead(@Param("convId") Long convId, @Param("userId") int userId);
}

package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group3.computer.sba301_computershop.entity.ChatConversation;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByRoomKey(String roomKey);

    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.user1 u1 JOIN FETCH u1.role JOIN FETCH c.user2 u2 JOIN FETCH u2.role WHERE c.user1.userId = :userId OR c.user2.userId = :userId ORDER BY c.lastMessageAt DESC")
    List<ChatConversation> findAllByUserId(@Param("userId") int userId);
}

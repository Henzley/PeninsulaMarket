package za.ac.cput.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.marketplace.domain.DirectMessage;
import java.time.Instant;
import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    @Query("""
        select m from DirectMessage m
        join fetch m.sender
        where m.conversation.id = :conversationId
        order by m.createdAt asc, m.id asc
        """)
    List<DirectMessage> findHistory(@Param("conversationId") Long conversationId);

    long countByConversation_IdAndSender_IdNotAndReadAtIsNull(Long conversationId, Long userId);

    @Modifying
    @Query("""
        update DirectMessage m set m.readAt = :readAt
        where m.conversation.id = :conversationId
          and m.sender.id <> :userId and m.readAt is null
        """)
    int markReceivedMessagesRead(@Param("conversationId") Long conversationId,
                                 @Param("userId") Long userId,
                                 @Param("readAt") Instant readAt);
}
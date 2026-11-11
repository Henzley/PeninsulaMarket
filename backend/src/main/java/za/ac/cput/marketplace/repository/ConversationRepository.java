package za.ac.cput.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.marketplace.domain.Conversation;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByListing_IdAndBuyer_Id(Long listingId, Long buyerId);

    @Query("""
        select c from Conversation c
        join fetch c.listing l
        join fetch c.buyer b
        join fetch c.seller s
        where b.id = :userId or s.id = :userId
        order by c.updatedAt desc
        """)
    List<Conversation> findForParticipant(@Param("userId") Long userId);

    @Query("""
        select c from Conversation c
        join fetch c.listing
        join fetch c.buyer
        join fetch c.seller
        where c.id = :id
        """)
    Optional<Conversation> findDetailedById(@Param("id") Long id);
}
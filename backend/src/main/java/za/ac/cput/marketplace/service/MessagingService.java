package za.ac.cput.marketplace.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.marketplace.domain.Conversation;
import za.ac.cput.marketplace.domain.DirectMessage;
import za.ac.cput.marketplace.domain.Listing;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.ConversationRepository;
import za.ac.cput.marketplace.repository.DirectMessageRepository;
import za.ac.cput.marketplace.repository.ListingRepository;
import za.ac.cput.marketplace.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
public class MessagingService {
    private final ConversationRepository conversations;
    private final DirectMessageRepository messages;
    private final ListingRepository listings;
    private final UserRepository users;

    public MessagingService(ConversationRepository conversations,
                            DirectMessageRepository messages,
                            ListingRepository listings,
                            UserRepository users) {
        this.conversations = conversations;
        this.messages = messages;
        this.listings = listings;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Conversation> listConversations(Long userId) {
        return conversations.findForParticipant(userId);
    }

    @Transactional
    public Conversation startConversation(Long buyerId, String loginAs, Long listingId) {
        requireMode(loginAs, "BUYER");
        Listing listing = listings.findById(listingId)
                .orElseThrow(() -> AdminException.notFound("Listing not found"));
        if (listing.getStatus() != Listing.Status.APPROVED) {
            throw AdminException.notFound("Listing is not available for messaging");
        }
        if (listing.getSeller() == null || listing.getSeller().getId().equals(buyerId)) {
            throw AdminException.conflict("You cannot message yourself about your own listing");
        }

        return conversations.findByListing_IdAndBuyer_Id(listingId, buyerId)
                .orElseGet(() -> {
                    User buyer = users.findById(buyerId)
                            .orElseThrow(() -> AdminException.unauthorized("Account not found"));
                    Conversation conversation = new Conversation();
                    conversation.setListing(listing);
                    conversation.setBuyer(buyer);
                    conversation.setSeller(listing.getSeller());
                    return conversations.save(conversation);
                });
    }

    @Transactional(readOnly = true)
    public List<DirectMessage> getMessages(Long userId, Long conversationId) {
        requireParticipant(userId, conversationId);
        return messages.findHistory(conversationId);
    }

    @Transactional
    public DirectMessage sendMessage(Long userId, String loginAs, Long conversationId, String content) {
        requireMode(loginAs, "BUYER", "SELLER");
        Conversation conversation = requireParticipant(userId, conversationId);
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.isEmpty()) {
            throw AdminException.conflict("Message cannot be empty");
        }
        if (cleaned.length() > 2000) {
            throw AdminException.conflict("Messages must be 2000 characters or fewer");
        }

        User sender = users.findById(userId)
                .orElseThrow(() -> AdminException.unauthorized("Account not found"));
        DirectMessage message = new DirectMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(cleaned);
        message.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(message.getCreatedAt());
        conversations.save(conversation);
        return messages.save(message);
    }

    @Transactional
    public void markRead(Long userId, Long conversationId) {
        requireParticipant(userId, conversationId);
        messages.markReceivedMessagesRead(conversationId, userId, Instant.now());
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId, Long conversationId) {
        requireParticipant(userId, conversationId);
        return messages.countByConversation_IdAndSender_IdNotAndReadAtIsNull(conversationId, userId);
    }

    private Conversation requireParticipant(Long userId, Long conversationId) {
        Conversation conversation = conversations.findDetailedById(conversationId)
                .orElseThrow(() -> AdminException.notFound("Conversation not found"));
        if (!conversation.getBuyer().getId().equals(userId)
                && !conversation.getSeller().getId().equals(userId)) {
            throw AdminException.forbidden("You are not a participant in this conversation");
        }
        return conversation;
    }

    private void requireMode(String actual, String... allowed) {
        for (String mode : allowed) {
            if (mode.equalsIgnoreCase(actual == null ? "" : actual)) return;
        }
        throw AdminException.forbidden("Your current account mode cannot perform this action");
    }
}
package za.ac.cput.marketplace.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.marketplace.domain.Conversation;
import za.ac.cput.marketplace.domain.DirectMessage;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.UserRepository;
import za.ac.cput.marketplace.service.MessagingService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessagingController {
    private final MessagingService messagingService;
    private final UserRepository users;

    public MessagingController(MessagingService messagingService, UserRepository users) {
        this.messagingService = messagingService;
        this.users = users;
    }

    @GetMapping("/conversations")
    public List<ConversationView> conversations(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return messagingService.listConversations(userId).stream()
                .map(c -> toView(c, userId))
                .toList();
    }

    @PostMapping("/conversations")
    public ConversationView start(@Valid @RequestBody StartRequest body, HttpServletRequest request) {
        Long userId = requireUserId(request);
        Conversation conversation = messagingService.startConversation(
                userId, (String) request.getAttribute("loginAs"), body.listingId());
        return toView(conversation, userId);
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageView> history(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        return messagingService.getMessages(userId, id).stream().map(this::toView).toList();
    }

    @PostMapping("/conversations/{id}/messages")
    public MessageView send(@PathVariable Long id, @Valid @RequestBody SendRequest body,
                            HttpServletRequest request) {
        Long userId = requireUserId(request);
        DirectMessage message = messagingService.sendMessage(
                userId, (String) request.getAttribute("loginAs"), id, body.content());
        return toView(message);
    }

    @PutMapping("/conversations/{id}/read")
    public void markRead(@PathVariable Long id, HttpServletRequest request) {
        messagingService.markRead(requireUserId(request), id);
    }

    private Long requireUserId(HttpServletRequest request) {
        Object raw = request.getAttribute("userId");
        if (raw == null) throw AdminException.unauthorized("Sign in to access messages");
        try {
            Long id = Long.valueOf(raw.toString());
            if (!users.existsById(id)) throw AdminException.unauthorized("Account not found");
            return id;
        } catch (NumberFormatException ex) {
            throw AdminException.unauthorized("Invalid session");
        }
    }

    private ConversationView toView(Conversation c, Long userId) {
        boolean buyer = c.getBuyer().getId().equals(userId);
        User other = buyer ? c.getSeller() : c.getBuyer();
        return new ConversationView(c.getId(), c.getListing().getId(), c.getListing().getTitle(),
                c.getListing().getPrice(), other.getId(), other.getFullName(),
                buyer ? "SELLER" : "BUYER", c.getUpdatedAt(),
                messagingService.unreadCount(userId, c.getId()));
    }

    private MessageView toView(DirectMessage m) {
        return new MessageView(m.getId(), m.getConversation().getId(), m.getSender().getId(),
                m.getSender().getFullName(), m.getContent(), m.getCreatedAt(), m.getReadAt());
    }

    public record StartRequest(@NotNull Long listingId) {}
    public record SendRequest(@NotBlank String content) {}
    public record ConversationView(Long id, Long listingId, String listingTitle,
                                   java.math.BigDecimal listingPrice, Long otherUserId,
                                   String otherUserName, String otherUserMode,
                                   Instant updatedAt, long unreadCount) {}
    public record MessageView(Long id, Long conversationId, Long senderId, String senderName,
                              String content, Instant createdAt, Instant readAt) {}
}
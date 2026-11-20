package com.chatnova.api.service;

import com.chatnova.api.dto.ChatDtos.ConversationResponse;
import com.chatnova.api.dto.ChatDtos.MessageResponse;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InMemoryChatService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final AtomicLong sequence = new AtomicLong(100);
    private final List<ConversationRecord> conversations = new CopyOnWriteArrayList<>();

    public InMemoryChatService() {
	        conversations.add(new ConversationRecord("maya", "direct", "Maya Chen", "online", "MC", "violet", true, true, 2, "The new orbit looks perfect.", "10:42", false, null,
	                new CopyOnWriteArrayList<>(List.of(
	                        new MessageRecord("m1", "maya", "Maya Chen", "MC", "Morning! I pushed the latest onboarding notes. The new orbit looks perfect.", "10:38", false, "read", "Today"),
	                        new MessageRecord("m2", "me", "Vasu Agarwal", "VA", "That is such a relief. I’ll read through them before lunch and leave comments.", "10:40", true, "read", null),
	                        new MessageRecord("m3", "maya", "Maya Chen", "MC", "Amazing. I also added a tiny checklist for the empty states.", "10:42", false, "read", null)))));
	        conversations.add(new ConversationRecord("studio", "group", "Studio North", "8 members", "SN", "mint", true, false, 5, "Nikhil: Let’s keep the blue accent.", "09:18", false, 8,
	                new CopyOnWriteArrayList<>(List.of(
	                        new MessageRecord("s1", "nikhil", "Nikhil Shah", "NS", "The new dashboard is feeling much lighter.", "09:12", false, "read", "Today"),
	                        new MessageRecord("s2", "me", "Vasu Agarwal", "VA", "Let’s keep the blue accent. It gives the navigation a good anchor.", "09:18", true, "delivered", null)))));
        conversations.add(new ConversationRecord("launch", "group", "Launch Crew", "14 members", "LC", "coral", false, false, 0, "You: Draft is ready for review", "Yesterday", false, 14,
                new CopyOnWriteArrayList<>(List.of(new MessageRecord("l1", "me", "Vasu Agarwal", "VA", "Draft is ready for review. Dropping the link here next.", "18:10", true, "read", "Yesterday")))));
    }

    public List<ConversationResponse> list(String query) {
        return conversations.stream()
                .filter(item -> query == null || query.isBlank() || (item.name + item.lastMessage).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                .map(this::toResponse)
                .toList();
    }

    public ConversationResponse get(String id) {
        return toResponse(find(id));
    }

    public List<MessageResponse> messages(String id) {
        return find(id).messages.stream().map(this::toResponse).toList();
    }

    public MessageResponse addMessage(String id, String content) {
        ConversationRecord conversation = find(id);
        Instant now = Instant.now();
        MessageRecord message = new MessageRecord("local-" + sequence.incrementAndGet(), "me", "Vasu Agarwal", "VA", content.trim(), TIME.format(LocalTime.now(ZoneId.systemDefault())), true, "read", null);
        conversation.messages.add(message);
        conversation.lastMessage = content.trim();
        conversation.lastTime = "now";
        return toResponse(message);
    }

    private ConversationRecord find(String id) {
        return conversations.stream().filter(item -> item.id.equals(id)).findFirst().orElseThrow(() -> new NoSuchElementException("Conversation not found: " + id));
    }

    private ConversationResponse toResponse(ConversationRecord item) {
        List<MessageResponse> messages = item.messages.stream().sorted(Comparator.comparing(message -> message.id)).map(this::toResponse).toList();
        return new ConversationResponse(item.id, item.kind, item.name, item.subtitle, item.avatar, item.avatarTone, item.online, item.pinned, item.unreadCount, item.lastMessage, item.lastTime, item.muted, item.members, messages);
    }

    private MessageResponse toResponse(MessageRecord item) {
        return new MessageResponse(item.id, item.senderId, item.senderName, item.senderAvatar, item.content, item.time, item.isMine, item.status, item.dateLabel);
    }

    private static final class ConversationRecord {
        private final String id, kind, name, subtitle, avatar, avatarTone;
        private final boolean online, pinned, muted;
        private final int unreadCount;
        private volatile String lastMessage, lastTime;
        private final Integer members;
        private final List<MessageRecord> messages;
        private ConversationRecord(String id, String kind, String name, String subtitle, String avatar, String avatarTone, boolean online, boolean pinned, int unreadCount, String lastMessage, String lastTime, boolean muted, Integer members, List<MessageRecord> messages) {
            this.id = id; this.kind = kind; this.name = name; this.subtitle = subtitle; this.avatar = avatar; this.avatarTone = avatarTone; this.online = online; this.pinned = pinned; this.unreadCount = unreadCount; this.lastMessage = lastMessage; this.lastTime = lastTime; this.muted = muted; this.members = members; this.messages = messages;
        }
    }

    private record MessageRecord(String id, String senderId, String senderName, String senderAvatar, String content, String time, boolean isMine, String status, String dateLabel) {}
}

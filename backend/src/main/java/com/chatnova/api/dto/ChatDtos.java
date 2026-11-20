package com.chatnova.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class ChatDtos {
    private ChatDtos() {}

    public record ConversationResponse(
            String id, String kind, String name, String subtitle, String avatar, String avatarTone,
            boolean online, boolean pinned, int unreadCount, String lastMessage, String lastTime,
            boolean muted, Integer members, List<MessageResponse> messages) {}

    public record MessageResponse(
            String id, String senderId, String senderName, String senderAvatar, String content,
            String time, boolean isMine, String status, String dateLabel) {}

    public record CreateMessageRequest(
            @NotBlank(message = "Message content cannot be empty")
            @Size(max = 10000, message = "Message content cannot exceed 10,000 characters")
            String content) {}

    public record HealthResponse(String status, String service, Instant timestamp) {}
    public record ErrorResponse(String code, String message, Instant timestamp) {}
}

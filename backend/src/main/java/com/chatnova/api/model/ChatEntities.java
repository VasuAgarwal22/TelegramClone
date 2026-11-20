package com.chatnova.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

public final class ChatEntities {
    private ChatEntities() {}

    @Entity
    @Table(name = "app_users")
    public static class AppUser {
        @Id
        private String id;
        @Column(nullable = false)
        private String displayName;
        @Column(nullable = false, unique = true)
        private String handle;
        private String avatar;
        private Instant createdAt;
        protected AppUser() {}
    }

    @Entity
    @Table(name = "conversations")
    public static class ConversationEntity {
        @Id
        private String id;
        @Column(nullable = false)
        private String kind;
        @Column(nullable = false)
        private String name;
        private String subtitle;
        private String avatar;
        private String avatarTone;
        private Instant createdAt;
        protected ConversationEntity() {}
    }

    @Entity
    @Table(name = "conversation_members")
    public static class ConversationMember {
        @Id
        private String id;
        @Column(nullable = false)
        private String conversationId;
        @Column(nullable = false)
        private String userId;
        private String role;
        private Instant joinedAt;
        protected ConversationMember() {}
    }

    @Entity
    @Table(name = "chat_messages")
    public static class ChatMessageEntity {
        @Id
        private String id;
        @Column(nullable = false)
        private String conversationId;
        @Column(nullable = false)
        private String senderId;
        @Column(nullable = false, length = 10000)
        private String content;
        private Instant sentAt;
        private Instant editedAt;
        protected ChatMessageEntity() {}
    }
}

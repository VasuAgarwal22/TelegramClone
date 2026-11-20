package com.chatnova.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

public final class FeatureEntities {
    private FeatureEntities() {}

    @Entity
    @Table(name = "smart_memory_settings")
    public static class MemorySettings {
        @Id private String ownerId;
        @Column(nullable = false) private boolean enabled = true;
        private Instant updatedAt;
        protected MemorySettings() {}
        public MemorySettings(String ownerId) { this.ownerId = ownerId; this.updatedAt = Instant.now(); }
        public String getOwnerId() { return ownerId; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; this.updatedAt = Instant.now(); }
    }

    @Entity
    @Table(name = "smart_memories")
    public static class MemoryRecord {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false) private String ownerId;
        @Column(nullable = false) private String conversationId;
        @Column(nullable = false) private String messageId;
        @Column(nullable = false) private String kind;
        @Column(nullable = false, length = 2000) private String title;
        @Column(name = "memory_value", nullable = false, length = 10000) private String value;
        @Column(nullable = false, length = 10000) private String sourceSnippet;
        private Instant createdAt;
        protected MemoryRecord() {}
        public MemoryRecord(String ownerId, String conversationId, String messageId, String kind, String title, String value, String sourceSnippet) { this.ownerId = ownerId; this.conversationId = conversationId; this.messageId = messageId; this.kind = kind; this.title = title; this.value = value; this.sourceSnippet = sourceSnippet; this.createdAt = Instant.now(); }
        public Long getId() { return id; }
        public String getOwnerId() { return ownerId; }
        public String getConversationId() { return conversationId; }
        public String getMessageId() { return messageId; }
        public String getKind() { return kind; }
        public String getTitle() { return title; }
        public String getValue() { return value; }
        public String getSourceSnippet() { return sourceSnippet; }
    }

    @Entity
    @Table(name = "vault_settings")
    public static class VaultSettings {
        @Id private String ownerId;
        @Column(length = 500) private String pinHash;
        @Column(length = 500) private String pinSalt;
        @Column(nullable = false) private boolean locked = true;
        @Column(nullable = false) private boolean biometricEnabled;
        private Instant updatedAt;
        protected VaultSettings() {}
        public VaultSettings(String ownerId) { this.ownerId = ownerId; this.updatedAt = Instant.now(); }
        public String getOwnerId() { return ownerId; }
        public String getPinHash() { return pinHash; }
        public String getPinSalt() { return pinSalt; }
        public boolean isLocked() { return locked; }
        public boolean isBiometricEnabled() { return biometricEnabled; }
        public void configurePin(String hash, String salt) { this.pinHash = hash; this.pinSalt = salt; this.locked = false; this.updatedAt = Instant.now(); }
        public void setLocked(boolean locked) { this.locked = locked; this.updatedAt = Instant.now(); }
        public void setBiometricEnabled(boolean enabled) { this.biometricEnabled = enabled; this.updatedAt = Instant.now(); }
    }

    @Entity
    @Table(name = "vault_folders")
    public static class VaultFolder {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false) private String ownerId;
        @Column(nullable = false) private String name;
        private Instant createdAt;
        protected VaultFolder() {}
        public VaultFolder(String ownerId, String name) { this.ownerId = ownerId; this.name = name; this.createdAt = Instant.now(); }
        public Long getId() { return id; }
        public String getOwnerId() { return ownerId; }
        public String getName() { return name; }
    }

    @Entity
    @Table(name = "vault_items")
    public static class VaultItem {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false) private String ownerId;
        private Long folderId;
        @Column(nullable = false) private String type;
        @Column(nullable = false) private String title;
        @Column(nullable = false, length = 20000) private String encryptedContent;
        @Column(length = 10000) private String metadata;
        @Column(nullable = false) private String expiration;
        private Instant expiresAt;
        private Instant createdAt;
        @Column(nullable = false) private boolean archived;
        protected VaultItem() {}
        public VaultItem(String ownerId, Long folderId, String type, String title, String encryptedContent, String metadata, String expiration, Instant expiresAt) { this.ownerId = ownerId; this.folderId = folderId; this.type = type; this.title = title; this.encryptedContent = encryptedContent; this.metadata = metadata; this.expiration = expiration; this.expiresAt = expiresAt; this.createdAt = Instant.now(); }
        public Long getId() { return id; }
        public String getOwnerId() { return ownerId; }
        public Long getFolderId() { return folderId; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getEncryptedContent() { return encryptedContent; }
        public String getMetadata() { return metadata; }
        public String getExpiration() { return expiration; }
        public Instant getExpiresAt() { return expiresAt; }
        public Instant getCreatedAt() { return createdAt; }
        public boolean isArchived() { return archived; }
        public void setArchived(boolean archived) { this.archived = archived; }
    }
}

package com.chatnova.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class FeatureDtos {
    private FeatureDtos() {}
    public record MemorySettingsResponse(boolean enabled, int memoryCount) {}
    public record MemorySettingsRequest(boolean enabled) {}
    public record MemoryRecordResponse(Long id, String kind, String title, String value, String conversationId, String messageId, String sourceSnippet) {}
    public record MemorySearchResponse(String answer, List<MemoryRecordResponse> references) {}
    public record MemorySummaryResponse(String conversationId, String summary, List<MemoryRecordResponse> references) {}
    public record VaultSettingsResponse(boolean pinConfigured, boolean locked, boolean biometricEnabled) {}
    public record PinRequest(@NotBlank @Pattern(regexp = "\\d{4,8}", message = "PIN must contain 4 to 8 digits") String pin) {}
    public record BiometricRequest(boolean enabled) {}
    public record VaultFolderRequest(@NotBlank @Size(max = 80) String name) {}
    public record VaultFolderResponse(Long id, String name) {}
    public record VaultItemRequest(Long folderId, @NotBlank String type, @NotBlank @Size(max = 160) String title, @Size(max = 200000) String content, String metadata, @NotBlank String expiration) {}
    public record VaultItemResponse(Long id, Long folderId, String type, String title, String content, String metadata, String expiration, Instant expiresAt, Instant createdAt, boolean archived) {}
}

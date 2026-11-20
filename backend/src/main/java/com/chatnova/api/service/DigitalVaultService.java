package com.chatnova.api.service;

import com.chatnova.api.config.FeatureConfig.CurrentUser;
import com.chatnova.api.config.FeatureConfig.VaultCipher;
import com.chatnova.api.dto.FeatureDtos.BiometricRequest;
import com.chatnova.api.dto.FeatureDtos.PinRequest;
import com.chatnova.api.dto.FeatureDtos.VaultFolderRequest;
import com.chatnova.api.dto.FeatureDtos.VaultFolderResponse;
import com.chatnova.api.dto.FeatureDtos.VaultItemRequest;
import com.chatnova.api.dto.FeatureDtos.VaultItemResponse;
import com.chatnova.api.dto.FeatureDtos.VaultSettingsResponse;
import com.chatnova.api.model.FeatureEntities.VaultFolder;
import com.chatnova.api.model.FeatureEntities.VaultItem;
import com.chatnova.api.model.FeatureEntities.VaultSettings;
import com.chatnova.api.repository.VaultFolderRepository;
import com.chatnova.api.repository.VaultItemRepository;
import com.chatnova.api.repository.VaultSettingsRepository;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigitalVaultService {
    private final VaultSettingsRepository settings;
    private final VaultFolderRepository folders;
    private final VaultItemRepository items;
    private final VaultCipher cipher;
    private final CurrentUser currentUser;
    private final SecureRandom random = new SecureRandom();

    public DigitalVaultService(VaultSettingsRepository settings, VaultFolderRepository folders, VaultItemRepository items, VaultCipher cipher, CurrentUser currentUser) { this.settings = settings; this.folders = folders; this.items = items; this.cipher = cipher; this.currentUser = currentUser; }
    private VaultSettings state() { return settings.findById(currentUser.id()).orElseGet(() -> settings.save(new VaultSettings(currentUser.id()))); }
    public VaultSettingsResponse settings() { VaultSettings state = state(); return new VaultSettingsResponse(state.getPinHash() != null, state.isLocked(), state.isBiometricEnabled()); }
    @Transactional public void configurePin(PinRequest request) { VaultSettings state = state(); byte[] salt = new byte[16]; random.nextBytes(salt); state.configurePin(hash(request.pin(), salt), Base64.getEncoder().encodeToString(salt)); settings.save(state); }
    @Transactional public boolean unlock(PinRequest request) { VaultSettings state = state(); if (state.getPinHash() == null || !hash(request.pin(), Base64.getDecoder().decode(state.getPinSalt())).equals(state.getPinHash())) return false; state.setLocked(false); settings.save(state); return true; }
    @Transactional public void lock() { VaultSettings state = state(); state.setLocked(true); settings.save(state); }
    @Transactional public void setBiometric(BiometricRequest request) { VaultSettings state = state(); state.setBiometricEnabled(request.enabled()); settings.save(state); }
    public List<VaultFolderResponse> folders() { return folders.findByOwnerIdOrderByNameAsc(currentUser.id()).stream().map(folder -> new VaultFolderResponse(folder.getId(), folder.getName())).toList(); }
    @Transactional public VaultFolderResponse createFolder(VaultFolderRequest request) { VaultFolder folder = folders.save(new VaultFolder(currentUser.id(), request.name().trim())); return new VaultFolderResponse(folder.getId(), folder.getName()); }
    public List<VaultItemResponse> items(boolean archived) { requireUnlocked(); return items.findByOwnerIdAndArchivedOrderByCreatedAtDesc(currentUser.id(), archived).stream().map(this::toResponse).toList(); }
    @Transactional public VaultItemResponse createItem(VaultItemRequest request) { requireUnlocked(); if (request.folderId() != null) folders.findByIdAndOwnerId(request.folderId(), currentUser.id()).orElseThrow(() -> new IllegalArgumentException("Folder not found")); Instant expiresAt = expiration(request.expiration()); VaultItem item = items.save(new VaultItem(currentUser.id(), request.folderId(), request.type(), request.title().trim(), cipher.encrypt(request.content()), request.metadata(), request.expiration(), expiresAt)); return toResponse(item); }
    @Transactional public void archive(Long id) { requireUnlocked(); VaultItem item = owned(id); item.setArchived(true); items.save(item); }
    @Transactional public void restore(Long id) { requireUnlocked(); VaultItem item = owned(id); item.setArchived(false); items.save(item); }
    @Transactional public void delete(Long id) { requireUnlocked(); items.delete(owned(id)); }
    @Scheduled(fixedDelayString = "${chatnova.vault.cleanup-delay-ms:60000}") @Transactional public void deleteExpired() { items.deleteAll(items.findByExpiresAtBefore(Instant.now())); }
    private VaultItem owned(Long id) { return items.findByIdAndOwnerId(id, currentUser.id()).orElseThrow(() -> new IllegalArgumentException("Vault item not found")); }
    private void requireUnlocked() { VaultSettings state = state(); if (state.getPinHash() != null && state.isLocked()) throw new IllegalStateException("Digital Vault is locked"); }
    private VaultItemResponse toResponse(VaultItem item) { return new VaultItemResponse(item.getId(), item.getFolderId(), item.getType(), item.getTitle(), cipher.decrypt(item.getEncryptedContent()), item.getMetadata(), item.getExpiration(), item.getExpiresAt(), item.getCreatedAt(), item.isArchived()); }
    private Instant expiration(String value) { return switch (value.toLowerCase()) { case "1h", "1hour", "1 hour" -> Instant.now().plus(Duration.ofHours(1)); case "24h", "24hours", "24 hours" -> Instant.now().plus(Duration.ofHours(24)); case "7d", "7days", "7 days" -> Instant.now().plus(Duration.ofDays(7)); case "never" -> null; default -> throw new IllegalArgumentException("Expiration must be 1 hour, 24 hours, 7 days, or never"); }; }
    private String hash(String pin, byte[] salt) { try { PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 120000, 256); return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded()); } catch (Exception exception) { throw new IllegalStateException("Unable to hash vault PIN", exception); } }
}

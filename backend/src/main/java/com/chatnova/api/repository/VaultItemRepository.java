package com.chatnova.api.repository;

import com.chatnova.api.model.FeatureEntities.VaultItem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {
    List<VaultItem> findByOwnerIdAndArchivedOrderByCreatedAtDesc(String ownerId, boolean archived);
    Optional<VaultItem> findByIdAndOwnerId(Long id, String ownerId);
    List<VaultItem> findByExpiresAtBefore(Instant now);
}

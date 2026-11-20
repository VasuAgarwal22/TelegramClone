package com.chatnova.api.repository;

import com.chatnova.api.model.FeatureEntities.VaultFolder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultFolderRepository extends JpaRepository<VaultFolder, Long> {
    List<VaultFolder> findByOwnerIdOrderByNameAsc(String ownerId);
    Optional<VaultFolder> findByIdAndOwnerId(Long id, String ownerId);
}

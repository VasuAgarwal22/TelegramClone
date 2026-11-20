package com.chatnova.api.repository;

import com.chatnova.api.model.FeatureEntities.VaultSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultSettingsRepository extends JpaRepository<VaultSettings, String> {}

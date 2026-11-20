package com.chatnova.api.repository;

import com.chatnova.api.model.FeatureEntities.MemorySettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemorySettingsRepository extends JpaRepository<MemorySettings, String> {}

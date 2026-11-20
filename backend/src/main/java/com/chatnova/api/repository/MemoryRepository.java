package com.chatnova.api.repository;

import com.chatnova.api.model.FeatureEntities.MemoryRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryRepository extends JpaRepository<MemoryRecord, Long> {
    List<MemoryRecord> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
    List<MemoryRecord> findByOwnerIdAndConversationId(String ownerId, String conversationId);
    Optional<MemoryRecord> findByIdAndOwnerId(Long id, String ownerId);
    void deleteByOwnerIdAndConversationId(String ownerId, String conversationId);
}

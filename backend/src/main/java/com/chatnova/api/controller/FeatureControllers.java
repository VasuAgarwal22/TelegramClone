package com.chatnova.api.controller;

import com.chatnova.api.dto.FeatureDtos.BiometricRequest;
import com.chatnova.api.dto.FeatureDtos.MemorySearchResponse;
import com.chatnova.api.dto.FeatureDtos.MemorySettingsRequest;
import com.chatnova.api.dto.FeatureDtos.MemorySettingsResponse;
import com.chatnova.api.dto.FeatureDtos.MemorySummaryResponse;
import com.chatnova.api.dto.FeatureDtos.PinRequest;
import com.chatnova.api.dto.FeatureDtos.VaultFolderRequest;
import com.chatnova.api.dto.FeatureDtos.VaultFolderResponse;
import com.chatnova.api.dto.FeatureDtos.VaultItemRequest;
import com.chatnova.api.dto.FeatureDtos.VaultItemResponse;
import com.chatnova.api.dto.FeatureDtos.VaultSettingsResponse;
import com.chatnova.api.service.DigitalVaultService;
import com.chatnova.api.service.SmartMemoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memory")
class SmartMemoryController {
    private final SmartMemoryService service;
    SmartMemoryController(SmartMemoryService service) { this.service = service; }
    @GetMapping("/settings") MemorySettingsResponse settings() { return service.settings(); }
    @PutMapping("/settings") MemorySettingsResponse setSettings(@Valid @RequestBody MemorySettingsRequest request) { return service.setEnabled(request.enabled()); }
    @PostMapping("/index/{conversationId}") Map<String, Integer> index(@PathVariable String conversationId) { return Map.of("indexed", service.index(conversationId)); }
    @GetMapping("/search") MemorySearchResponse search(@RequestParam String q) { return service.search(q); }
    @GetMapping("/summary/{conversationId}") MemorySummaryResponse summary(@PathVariable String conversationId) { return service.summary(conversationId); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable Long id) { service.delete(id); }
}

@RestController
@RequestMapping("/api/vault")
class DigitalVaultController {
    private final DigitalVaultService service;
    DigitalVaultController(DigitalVaultService service) { this.service = service; }
    @GetMapping("/settings") VaultSettingsResponse settings() { return service.settings(); }
    @PostMapping("/pin") @ResponseStatus(HttpStatus.NO_CONTENT) void pin(@Valid @RequestBody PinRequest request) { service.configurePin(request); }
    @PostMapping("/unlock") Map<String, Boolean> unlock(@Valid @RequestBody PinRequest request) { return Map.of("unlocked", service.unlock(request)); }
    @PostMapping("/lock") @ResponseStatus(HttpStatus.NO_CONTENT) void lock() { service.lock(); }
    @PutMapping("/biometric") @ResponseStatus(HttpStatus.NO_CONTENT) void biometric(@RequestBody BiometricRequest request) { service.setBiometric(request); }
    @GetMapping("/folders") List<VaultFolderResponse> folders() { return service.folders(); }
    @PostMapping("/folders") VaultFolderResponse createFolder(@Valid @RequestBody VaultFolderRequest request) { return service.createFolder(request); }
    @GetMapping("/items") List<VaultItemResponse> items(@RequestParam(defaultValue = "false") boolean archived) { return service.items(archived); }
    @PostMapping("/items") VaultItemResponse createItem(@Valid @RequestBody VaultItemRequest request) { return service.createItem(request); }
    @PostMapping("/items/{id}/archive") @ResponseStatus(HttpStatus.NO_CONTENT) void archive(@PathVariable Long id) { service.archive(id); }
    @PostMapping("/items/{id}/restore") @ResponseStatus(HttpStatus.NO_CONTENT) void restore(@PathVariable Long id) { service.restore(id); }
    @DeleteMapping("/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable Long id) { service.delete(id); }
}

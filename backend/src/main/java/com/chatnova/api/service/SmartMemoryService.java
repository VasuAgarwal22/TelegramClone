package com.chatnova.api.service;

import com.chatnova.api.config.FeatureConfig.CurrentUser;
import com.chatnova.api.dto.ChatDtos.MessageResponse;
import com.chatnova.api.dto.FeatureDtos.MemoryRecordResponse;
import com.chatnova.api.dto.FeatureDtos.MemorySearchResponse;
import com.chatnova.api.dto.FeatureDtos.MemorySettingsResponse;
import com.chatnova.api.dto.FeatureDtos.MemorySummaryResponse;
import com.chatnova.api.model.FeatureEntities.MemoryRecord;
import com.chatnova.api.model.FeatureEntities.MemorySettings;
import com.chatnova.api.repository.MemoryRepository;
import com.chatnova.api.repository.MemorySettingsRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmartMemoryService {
    private static final Pattern URL = Pattern.compile("https?://\\S+");
    private static final Pattern DATE = Pattern.compile("\\b(?:today|tomorrow|yesterday|next week|\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TASK = Pattern.compile("\\b(?:todo|task|remember|remind|need to|action item|follow up)\\b[^.!?]{0,120}", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMITMENT = Pattern.compile("\\b(?:I will|we will|I'll|we'll|committed to|promise to)\\b[^.!?]{0,120}", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCATION = Pattern.compile("\\b(?:in|at|near|from)\\s+[A-Z][\\w-]*(?:\\s+[A-Z][\\w-]*){0,2}");
    private final MemoryRepository memories;
    private final MemorySettingsRepository settings;
    private final InMemoryChatService chats;
    private final CurrentUser currentUser;

    public SmartMemoryService(MemoryRepository memories, MemorySettingsRepository settings, InMemoryChatService chats, CurrentUser currentUser) { this.memories = memories; this.settings = settings; this.chats = chats; this.currentUser = currentUser; }

    public MemorySettingsResponse settings() { MemorySettings state = settings.findById(currentUser.id()).orElseGet(() -> settings.save(new MemorySettings(currentUser.id()))); return new MemorySettingsResponse(state.isEnabled(), memories.findByOwnerIdOrderByCreatedAtDesc(currentUser.id()).size()); }
    @Transactional public MemorySettingsResponse setEnabled(boolean enabled) { MemorySettings state = settings.findById(currentUser.id()).orElseGet(() -> new MemorySettings(currentUser.id())); state.setEnabled(enabled); settings.save(state); return settings(); }

    @Transactional public int index(String conversationId) {
        if (!settings().enabled()) return 0;
        List<MessageResponse> messages = chats.messages(conversationId);
        memories.deleteByOwnerIdAndConversationId(currentUser.id(), conversationId);
        List<MemoryRecord> extracted = new ArrayList<>();
        for (MessageResponse message : messages) extracted.addAll(extract(conversationId, message));
        memories.saveAll(extracted);
        return extracted.size();
    }

    public MemorySearchResponse search(String question) {
        if (!settings().enabled()) return new MemorySearchResponse("Smart Memory is disabled. Enable it in the Memory panel to search your history.", List.of());
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT);
        List<String> terms = Arrays.stream(normalized.split("\\W+")).filter(term -> term.length() > 2).toList();
        List<MemoryRecord> results = memories.findByOwnerIdOrderByCreatedAtDesc(currentUser.id()).stream().filter(record -> terms.isEmpty() || terms.stream().anyMatch(term -> (record.getValue() + " " + record.getTitle() + " " + record.getSourceSnippet()).toLowerCase(Locale.ROOT).contains(term))).limit(12).toList();
        String answer = results.isEmpty() ? "I couldn't find a matching memory in your indexed conversations." : "I found " + results.size() + " relevant memory reference" + (results.size() == 1 ? "" : "s") + " from your conversations.";
        return new MemorySearchResponse(answer, results.stream().map(this::toResponse).toList());
    }

    public MemorySummaryResponse summary(String conversationId) {
        List<MessageResponse> messages = chats.messages(conversationId);
        String summary = messages.stream().map(MessageResponse::content).limit(8).reduce((left, right) -> left + " " + right).map(text -> text.length() > 480 ? text.substring(0, 480) + "…" : text).orElse("No messages to summarize.");
        return new MemorySummaryResponse(conversationId, summary, memories.findByOwnerIdAndConversationId(currentUser.id(), conversationId).stream().limit(12).map(this::toResponse).toList());
    }

    @Transactional public void delete(Long id) { memories.findByIdAndOwnerId(id, currentUser.id()).ifPresent(memories::delete); }

    private List<MemoryRecord> extract(String conversationId, MessageResponse message) {
        String content = message.content();
        List<MemoryRecord> output = new ArrayList<>();
        addMatches(output, conversationId, message, URL, "link", "Link");
        addMatches(output, conversationId, message, DATE, "date", "Date");
        addMatches(output, conversationId, message, TASK, "task", "Task");
        addMatches(output, conversationId, message, COMMITMENT, "commitment", "Commitment");
        addMatches(output, conversationId, message, LOCATION, "location", "Location");
        Set<String> names = new LinkedHashSet<>();
        if (!"me".equalsIgnoreCase(message.senderId()) && message.senderName() != null && !message.senderName().isBlank()) names.add(message.senderName());
        Matcher words = Pattern.compile("\\b[A-Z][a-z]{2,}\\s+[A-Z][a-z]{2,}\\b").matcher(content);
        while (words.find()) names.add(words.group());
        names.stream().limit(4).forEach(name -> output.add(new MemoryRecord(currentUser.id(), conversationId, message.id(), "name", "Name mentioned", name, content)));
        return output;
    }
    private void addMatches(List<MemoryRecord> output, String conversationId, MessageResponse message, Pattern pattern, String kind, String title) { Matcher matcher = pattern.matcher(message.content()); while (matcher.find()) { String value = matcher.group().trim(); output.add(new MemoryRecord(currentUser.id(), conversationId, message.id(), kind, title, value, message.content())); } }
    private MemoryRecordResponse toResponse(MemoryRecord record) { return new MemoryRecordResponse(record.getId(), record.getKind(), record.getTitle(), record.getValue(), record.getConversationId(), record.getMessageId(), record.getSourceSnippet()); }
}

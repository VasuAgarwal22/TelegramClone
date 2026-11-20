package com.chatnova.api.controller;

import com.chatnova.api.dto.ChatDtos.ConversationResponse;
import com.chatnova.api.dto.ChatDtos.CreateMessageRequest;
import com.chatnova.api.dto.ChatDtos.HealthResponse;
import com.chatnova.api.dto.ChatDtos.MessageResponse;
import com.chatnova.api.service.InMemoryChatService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatControllers {
    private final InMemoryChatService chatService;

    public ChatControllers(InMemoryChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", "chatnova-api", Instant.now());
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> conversations(@RequestParam(required = false) String query) {
        return chatService.list(query);
    }

    @GetMapping("/conversations/{conversationId}")
    public ConversationResponse conversation(@PathVariable String conversationId) {
        return chatService.get(conversationId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<MessageResponse> messages(@PathVariable String conversationId) {
        return chatService.messages(conversationId);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(@PathVariable String conversationId, @Valid @RequestBody CreateMessageRequest request) {
        return chatService.addMessage(conversationId, request.content());
    }
}

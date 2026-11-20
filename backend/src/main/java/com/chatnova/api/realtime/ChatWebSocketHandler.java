package com.chatnova.api.realtime;

import com.chatnova.api.dto.ChatDtos.MessageResponse;
import com.chatnova.api.service.InMemoryChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final InMemoryChatService chatService;
    private final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ObjectMapper objectMapper, InMemoryChatService chatService) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        subscriptions.put(session, ConcurrentHashMap.newKeySet());
        send(session, Map.of("type", "connected", "sessionId", session.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws IOException {
        JsonNode request = objectMapper.readTree(textMessage.getPayload());
        String type = request.path("type").asText();
        if ("subscribe".equals(type)) {
            String conversationId = request.path("conversationId").asText("");
            if (!conversationId.isBlank()) {
                chatService.messages(conversationId);
                subscriptions.computeIfAbsent(session, ignored -> ConcurrentHashMap.newKeySet()).add(conversationId);
                send(session, Map.of("type", "subscribed", "conversationId", conversationId));
            }
            return;
        }
        if ("unsubscribe".equals(type)) {
            String conversationId = request.path("conversationId").asText("");
            Set<String> subscribed = subscriptions.get(session);
            if (subscribed != null) subscribed.remove(conversationId);
            return;
        }
        if ("message".equals(type)) {
            String conversationId = request.path("conversationId").asText("");
            String content = request.path("content").asText("").trim();
            if (conversationId.isBlank() || content.isBlank()) {
                send(session, Map.of("type", "error", "message", "conversationId and content are required"));
                return;
            }
            if (!subscriptions.getOrDefault(session, Set.of()).contains(conversationId)) {
                send(session, Map.of("type", "error", "message", "Subscribe to the conversation before sending"));
                return;
            }
            MessageResponse message = chatService.addMessage(conversationId, content);
            Map<String, Object> event = Map.of(
                    "type", "message",
                    "conversationId", conversationId,
                    "clientMessageId", request.path("clientMessageId").asText(""),
                    "message", message);
            broadcast(conversationId, event);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        subscriptions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
    }

    private void broadcast(String conversationId, Map<String, Object> event) {
        subscriptions.forEach((session, subscribed) -> {
            if (session.isOpen() && subscribed.contains(conversationId)) {
                try { send(session, event); } catch (IOException ignored) { /* the close callback removes dead sessions */ }
            }
        });
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }
}

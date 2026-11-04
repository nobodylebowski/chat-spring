package com.example.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Component
public class ChatSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final Map<Integer, Set<WebSocketSession>> connections = new ConcurrentHashMap<>();

    public ChatSocketHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        Set<WebSocketSession> set =
                connections.computeIfAbsent(user.getId(), k -> new CopyOnWriteArraySet<>());
        boolean wasOffline = set.isEmpty();
        set.add(session);
        if (wasOffline) broadcastStatus(user.getId(), true);
        System.out.println(user.getName() + " connected");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        User user = (User) session.getAttributes().get("user");
        if (user == null) return;
        Set<WebSocketSession> set = connections.get(user.getId());
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) broadcastStatus(user.getId(), false);
        }
        System.out.println(user.getName() + " disconnected");
    }

    public Set<Integer> onlineUserIds() {
        return connections.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void pushMessage(Message m) {
            Map<String, Object> payload = Map.of(
                    "from", m.getSender().getId(),
                    "to", m.getReceiver().getId(),
                    "name", m.getSender().getName(),
                    "time", m.getTimeText(),
                    "content", m.getContent());
            String json = toJson(payload);
            sendToUser(m.getSender().getId(), json);
            sendToUser(m.getReceiver().getId(), json);
    }

    private void broadcastStatus(int userId, boolean online) {
        String json = toJson(Map.of("type", "status", "user", userId, "online", online));
        connections.values().forEach(set -> set.forEach(s -> send(s, json)));
    }

    private void sendToUser(int userId, String json) {
        Set<WebSocketSession> set = connections.get(userId);
        if (set == null) return;
        set.forEach(s -> send(s, json));
    }

    private void send(WebSocketSession s, String json) {
        if (!s.isOpen()) return;
        try {
            synchronized (s) {
                s.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toJson(Map<String, Object> payload) {
            return mapper.writeValueAsString(payload);

    }
}

package com.example.chat;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
public class MessageService {
    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public Message send(User sender, User receiver, String content) {
        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender and receiver cannot be null");
        }
        try {
        return repository.insert(sender, receiver, content);
        } catch (SQLException e) {
            throw new RuntimeException("Database error during message sending : " + e);
        }
    }

    public List<Message> getConversation(User a, User b){
        try{
                if (a == null || b == null ) {
                throw new IllegalArgumentException("Users cannot be null");
                }
        return repository.findMessageBetween(a, b);
        } catch (SQLException e) {
            throw new RuntimeException("Database error during message retrieving : " + e);
        }
    }
}

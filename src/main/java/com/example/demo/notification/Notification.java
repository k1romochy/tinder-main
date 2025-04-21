package com.example.demo.notification;

import com.example.demo.user.Repository.User;

import java.time.LocalDateTime;

public class Notification {
    private String senderId;
    private String receiverId;
    private String message;
    private LocalDateTime timestamp;
    private String type; // LIKE, MATCH, etc.

    public Notification(Long userId, Long userTargetId, String s, LocalDateTime now, String type) {
        this.senderId = String.valueOf(userId);
        this.receiverId = String.valueOf(userTargetId);
        this.message = s;
        this.timestamp = now;
        this.type = type;
    }
}

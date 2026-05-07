package com.appointmed.controller;

import com.appointmed.model.Message;
import com.appointmed.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired private MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Map<String, Object> body) {
        Long senderId = Long.valueOf(body.get("senderId").toString());
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = body.get("content").toString();
        return ResponseEntity.ok(messageService.sendMessage(senderId, receiverId, content));
    }

    @GetMapping("/conversations/{userId}")
    public List<Map<String, Object>> getConversations(@PathVariable Long userId) {
        return messageService.getConversationList(userId);
    }

    @GetMapping("/conversation/{user1}/{user2}")
    public List<Message> getConversation(@PathVariable Long user1, @PathVariable Long user2) {
        return messageService.getConversation(user1, user2);
    }

    @PutMapping("/read/{userId}/{partnerId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long userId, @PathVariable Long partnerId) {
        messageService.markConversationAsRead(userId, partnerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/{userId}")
    public Map<String, Long> getUnreadCount(@PathVariable Long userId) {
        return Map.of("count", messageService.getUnreadCount(userId));
    }
}

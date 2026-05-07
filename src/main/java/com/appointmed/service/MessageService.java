package com.appointmed.service;

import com.appointmed.model.Message;
import com.appointmed.model.User;
import com.appointmed.repository.MessageRepository;
import com.appointmed.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired private MessageRepository messageRepo;
    @Autowired private UserRepository userRepo;

    public Message sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message msg = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isRead(false)
                .build();
        return messageRepo.save(msg);
    }

    public List<Message> getConversation(Long user1, Long user2) {
        return messageRepo.findConversation(user1, user2);
    }

    /**
     * Returns a list of conversations (unique chat partners) for the given user,
     * each with the last message and partner info.
     */
    public List<Map<String, Object>> getConversationList(Long userId) {
        List<Message> allMessages = messageRepo.findAllByUserId(userId);

        // Group by conversation partner
        Map<Long, Message> latestByPartner = new LinkedHashMap<>();
        for (Message m : allMessages) {
            Long partnerId = m.getSender().getId().equals(userId)
                    ? m.getReceiver().getId()
                    : m.getSender().getId();
            latestByPartner.putIfAbsent(partnerId, m);
        }

        List<Map<String, Object>> conversations = new ArrayList<>();
        for (var entry : latestByPartner.entrySet()) {
            Long partnerId = entry.getKey();
            Message lastMsg = entry.getValue();
            User partner = lastMsg.getSender().getId().equals(userId)
                    ? lastMsg.getReceiver()
                    : lastMsg.getSender();

            Map<String, Object> conv = new HashMap<>();
            conv.put("partnerId", partnerId);
            conv.put("partnerName", partner.getName());
            conv.put("partnerRole", partner.getRole());
            conv.put("lastMessage", lastMsg.getContent());
            conv.put("lastMessageTime", lastMsg.getCreatedAt());
            conv.put("lastMessageSenderId", lastMsg.getSender().getId());
            conversations.add(conv);
        }
        return conversations;
    }

    public void markConversationAsRead(Long userId, Long partnerId) {
        List<Message> conversation = messageRepo.findConversation(userId, partnerId);
        for (Message m : conversation) {
            if (m.getReceiver().getId().equals(userId) && !m.isRead()) {
                m.setRead(true);
                messageRepo.save(m);
            }
        }
    }

    public long getUnreadCount(Long userId) {
        return messageRepo.countUnreadByReceiverId(userId);
    }
}

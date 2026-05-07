package com.appointmed.controller;

import com.appointmed.model.Notification;
import com.appointmed.model.User;
import com.appointmed.repository.UserRepository;
import com.appointmed.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public Map<String, Object> getUnread(@PathVariable Long userId) {
        return Map.of(
            "count", notificationService.getUnreadCount(userId),
            "notifications", notificationService.getUnreadNotifications(userId)
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Doctor sends a notification to a patient.
     * Body: { "patientId": Long, "doctorId": Long, "title": String, "message": String }
     */
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestBody Map<String, Object> body) {
        Long patientId = Long.valueOf(body.get("patientId").toString());
        Long doctorId = Long.valueOf(body.get("doctorId").toString());
        String title = body.get("title").toString();
        String message = body.get("message").toString();

        // Verify the sender is a doctor
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        if (!"DOCTOR".equals(doctor.getRole())) {
            return ResponseEntity.status(403).build();
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Notification n = notificationService.createNotification(patient, title, message, Notification.Type.REMINDER);
        return ResponseEntity.ok(n);
    }
}

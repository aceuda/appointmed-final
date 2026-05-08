package com.appointmed.controller;

import com.appointmed.dto.AuthResponse;
import com.appointmed.dto.ChangePasswordRequest;
import com.appointmed.model.User;
import com.appointmed.dto.LoginRequest;
import com.appointmed.dto.RegisterRequest;
import com.appointmed.service.UserService;
import com.appointmed.service.AppointmentService;
import com.appointmed.service.RecordService;
import com.appointmed.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AppointmentService appointmentService;
    @Autowired private RecordService recordService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        User u = userService.register(req);
        String token = jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId());
        return ResponseEntity.ok(AuthResponse.from(u, token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return userService.login(req)
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                    return ResponseEntity.ok(AuthResponse.from(user, token));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long id) {
        long unpaid = appointmentService.countUnpaidByPatientId(id);
        long records = recordService.countPatientRecords(id);
        long prescriptions = recordService.countActivePrescriptions(id);
        return ResponseEntity.ok(Map.of(
                "unpaidInvoices", unpaid,
                "medicalRecords", records,
                "activePrescriptions", prescriptions
        ));
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req) {
        try {
            userService.changePassword(id, req.getCurrentPassword(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

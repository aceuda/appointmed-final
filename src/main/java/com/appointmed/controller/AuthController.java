package com.appointmed.controller;

import com.appointmed.dto.AuthResponse;
import com.appointmed.dto.LoginRequest;
import com.appointmed.dto.RegisterRequest;
import com.appointmed.model.User;
import com.appointmed.service.UserService;
import com.appointmed.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({ "/api/auth", "/auth" })
@CrossOrigin(origins = { "http://localhost:3000", "https://appointmed-final.vercel.app" })
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        User user = userService.register(req);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        return ResponseEntity.ok(AuthResponse.from(user, token));
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
}

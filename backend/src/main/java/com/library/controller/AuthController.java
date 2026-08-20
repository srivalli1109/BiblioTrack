package com.library.controller;

import com.library.model.LoginRequest;
import com.library.model.User;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty()) {
            return "Invalid username or password";
        }

        User user = userOpt.get();

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return "Login successful! Welcome " + user.getUsername() + " (Role: " + user.getRole() + ")";
        } else {
            return "Invalid username or password";
        }
    }
}
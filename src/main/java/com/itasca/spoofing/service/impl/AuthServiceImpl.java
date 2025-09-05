package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.entity.UserEntity;
import com.itasca.spoofing.entity.UserRole;
import com.itasca.spoofing.repository.UserRepository;
import com.itasca.spoofing.security.JwtUtil;
import com.itasca.spoofing.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> signup(String email, String password, String firstName, String lastName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(email)
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .status("ACTIVE")
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", email);
        
        return Map.of("message", "User registered successfully");
    }

    @Override
    public Map<String, Object> signin(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Set<String> roles = user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());

        String token = jwtUtil.generateToken(user.getUsername(), roles);

        log.info("User signed in successfully: {}", email);

        return Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                        "lastName", user.getLastName() != null ? user.getLastName() : "",
                        "roles", roles,
                        "status", user.getStatus()
                )
        );
    }

    @Override
    public Map<String, Object> verifyAndRefreshToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (!jwtUtil.validateToken(token, username)) {
                throw new RuntimeException("Invalid or expired token");
            }

            UserEntity user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!"ACTIVE".equals(user.getStatus())) {
                throw new RuntimeException("Account is not active");
            }

            Set<String> roles = user.getRoles().stream()
                    .map(UserRole::name)
                    .collect(Collectors.toSet());

            String newToken = jwtUtil.generateToken(user.getUsername(), roles);

            log.info("Token refreshed for user: {}", username);

            return Map.of(
                    "token", newToken,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                            "lastName", user.getLastName() != null ? user.getLastName() : "",
                            "roles", roles,
                            "status", user.getStatus()
                    ),
                    "message", "Token refreshed successfully"
            );
        } catch (Exception e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw new RuntimeException("Token verification failed: " + e.getMessage());
        }
    }
}
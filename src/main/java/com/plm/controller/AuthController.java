package com.plm.controller;

import com.plm.dto.LoginRequest;
import com.plm.dto.LoginResponse;
import com.plm.entity.User;
import com.plm.repository.UserRepository;
import com.plm.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!user.getIsActive()) {
            log.warn("Inactive user login attempt: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String token = jwtUtil.generateToken(user.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();

        log.info("User {} logged in successfully", request.getUsername());
        return ResponseEntity.ok(response);
    }
}

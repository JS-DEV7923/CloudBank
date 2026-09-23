package com.cloudbank.service;

import static com.cloudbank.dto.ApiDtos.LoginResponse;
import static com.cloudbank.dto.ApiDtos.LoginUser;
import static com.cloudbank.dto.ApiDtos.RegisterRequest;
import static com.cloudbank.dto.ApiDtos.UserResponse;

import com.cloudbank.entity.UserEntity;
import com.cloudbank.exception.ApiException;
import com.cloudbank.repository.UserRepository;
import com.cloudbank.security.JwtService;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email is already registered");
        }
        UserEntity user = userRepository.save(new UserEntity(email, passwordEncoder.encode(request.password()), request.firstName(), request.lastName()));
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        UserEntity user = userRepository.findByEmail(normalizeEmail(email)).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return new LoginResponse(jwtService.issue(user), "Bearer", jwtService.getExpirationSeconds(), new LoginUser(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName()));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

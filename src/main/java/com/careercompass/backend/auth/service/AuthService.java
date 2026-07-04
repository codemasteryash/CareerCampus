package com.careercompass.backend.auth.service;

import com.careercompass.backend.auth.dto.AuthResponse;
import com.careercompass.backend.auth.dto.LoginRequest;
import com.careercompass.backend.auth.dto.RegisterRequest;
import com.careercompass.backend.exception.UserAlreadyExistsException;
import com.careercompass.backend.security.JwtService;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "An account with this email already exists.");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .targetJobRole(request.getTargetJobRole())
                .build();

        User savedUser = userRepository.save(user);
        UserPrincipal userPrincipal = new UserPrincipal(savedUser);
        String token = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(token)
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .targetJobRole(savedUser.getTargetJobRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "User authenticated but not found in database."));

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .targetJobRole(user.getTargetJobRole())
                .build();
    }
}
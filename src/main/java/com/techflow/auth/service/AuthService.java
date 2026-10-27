package com.techflow.auth.service;

import com.techflow.auth.dto.AuthResponse;
import com.techflow.auth.dto.LoginRequest;
import com.techflow.auth.dto.RefreshTokenRequest;
import com.techflow.auth.dto.RegisterRequest;
import com.techflow.auth.dto.UserResponse;
import com.techflow.common.exception.ConflictException;
import com.techflow.common.exception.UnauthorizedAccessException;
import com.techflow.security.jwt.JwtTokenProvider;
import com.techflow.security.user.CustomUserDetails;
import com.techflow.security.util.SecurityUtils;
import com.techflow.user.model.User;
import com.techflow.user.model.UserRole;
import com.techflow.user.model.UserStatus;
import com.techflow.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Email is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName().trim());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));

        return AuthResponse.of(
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpirationSeconds(),
                UserResponse.fromEntity(user)
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedAccessException("INVALID_TOKEN", "Refresh token is invalid or expired.");
        }

        UUID userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedAccessException("USER_NOT_FOUND", "User not found for token."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedAccessException("ACCOUNT_DISABLED", "Account is not active.");
        }

        CustomUserDetails userDetails = CustomUserDetails.fromEntity(user);
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getAccessTokenExpirationSeconds(),
                UserResponse.fromEntity(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UUID currentUserId = SecurityUtils.getRequiredCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));
        return UserResponse.fromEntity(user);
    }
}
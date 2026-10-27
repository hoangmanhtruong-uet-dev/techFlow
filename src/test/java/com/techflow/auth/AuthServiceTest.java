package com.techflow.auth;

import com.techflow.auth.dto.AuthResponse;
import com.techflow.auth.dto.LoginRequest;
import com.techflow.auth.dto.RegisterRequest;
import com.techflow.auth.dto.UserResponse;
import com.techflow.auth.service.AuthService;
import com.techflow.common.exception.ConflictException;
import com.techflow.security.jwt.JwtTokenProvider;
import com.techflow.security.user.CustomUserDetails;
import com.techflow.user.model.User;
import com.techflow.user.model.UserRole;
import com.techflow.user.model.UserStatus;
import com.techflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userDetails = CustomUserDetails.fromEntity(sampleUser);
    }

    @Test
    @DisplayName("Register - Thành công khi email chưa tồn tại")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("Test@Example.COM");
        request.setPassword("Password123!");
        request.setName("Test User");

        when(userRepository.existsByEmail("Test@Example.COM")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Ném ConflictException khi email đã tồn tại (BR-037)")
    void register_DuplicateEmail_ThrowsConflictException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login - Thành công khi thông tin đăng nhập đúng")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login - Ném BadCredentialsException khi sai mật khẩu")
    void login_WrongPassword_ThrowsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }
}
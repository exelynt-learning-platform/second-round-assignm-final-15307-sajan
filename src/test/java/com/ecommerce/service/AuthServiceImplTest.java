package com.ecommerce.service;

import com.ecommerce.config.JwtService;
import com.ecommerce.dto.requestDto.LoginRequestDto;
import com.ecommerce.dto.requestDto.RegisterRequestDto;
import com.ecommerce.dto.responseDto.AuthResponseDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import com.ecommerce.enumeration.Role;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.serviceImpl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private User user;
    private AuthResponseDto authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequestDto.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequestDto.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        authResponse = AuthResponseDto.builder()
                .token("jwt-token")
                .email("john@example.com")
                .fullName("John Doe")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void register_ShouldReturnAuthResponse_WhenEmailNotExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userMapper.toAuthResponseDto(any(User.class), anyString())).thenReturn(authResponse);

        AuthResponseDto result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_ShouldReturnAuthResponse_WhenCredentialsValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userMapper.toAuthResponseDto(any(User.class), anyString())).thenReturn(authResponse);

        AuthResponseDto result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}

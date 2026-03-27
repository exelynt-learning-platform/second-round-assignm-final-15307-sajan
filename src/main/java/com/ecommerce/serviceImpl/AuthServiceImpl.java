package com.ecommerce.serviceImpl;

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
import com.ecommerce.service.AuthService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Role DEFAULT_ROLE = Role.USER;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(DEFAULT_ROLE)
                .build();

        User savedUser = userRepository.save(user);

        Cart cart = Cart.builder()
                .user(savedUser)
                .build();
        cartRepository.save(cart);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", savedUser.getRole().name());
        extraClaims.put("fullName", savedUser.getFullName());
        
        String token = jwtService.generateToken(extraClaims, savedUser);
        return userMapper.toAuthResponseDto(savedUser, token);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found with email: " + request.getEmail()));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("fullName", user.getFullName());
        
        String token = jwtService.generateToken(extraClaims, user);
        return userMapper.toAuthResponseDto(user, token);
    }
}

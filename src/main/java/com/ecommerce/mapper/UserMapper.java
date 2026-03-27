package com.ecommerce.mapper;

import com.ecommerce.dto.responseDto.AuthResponseDto;
import com.ecommerce.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AuthResponseDto toAuthResponseDto(User user, String token) {
        return AuthResponseDto.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}

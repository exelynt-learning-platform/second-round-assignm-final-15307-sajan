package com.ecommerce.service;

import com.ecommerce.dto.requestDto.LoginRequestDto;
import com.ecommerce.dto.requestDto.RegisterRequestDto;
import com.ecommerce.dto.responseDto.AuthResponseDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}

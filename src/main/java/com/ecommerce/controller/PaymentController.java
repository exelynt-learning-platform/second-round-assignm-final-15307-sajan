package com.ecommerce.controller;

import com.ecommerce.dto.requestDto.PaymentVerificationRequestDto;
import com.ecommerce.dto.responseDto.ApiResponseDto;
import com.ecommerce.dto.responseDto.PaymentResponseDto;
import com.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> createPaymentOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        PaymentResponseDto response = paymentService.createPaymentOrder(authentication.getName(), orderId);
        return ResponseEntity.ok(ApiResponseDto.success("Payment order created successfully", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponseDto<String>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequestDto request) {
        String result = paymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }
}

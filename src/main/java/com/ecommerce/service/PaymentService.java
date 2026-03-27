package com.ecommerce.service;

import com.ecommerce.dto.requestDto.PaymentVerificationRequestDto;
import com.ecommerce.dto.responseDto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto createPaymentOrder(String userEmail, Long orderId);

    String verifyPayment(PaymentVerificationRequestDto request);
}

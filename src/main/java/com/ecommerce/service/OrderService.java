package com.ecommerce.service;

import com.ecommerce.dto.requestDto.OrderRequestDto;
import com.ecommerce.dto.responseDto.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(String userEmail, OrderRequestDto request);

    List<OrderResponseDto> getUserOrders(String userEmail);

    OrderResponseDto getOrderById(String userEmail, Long orderId);
}

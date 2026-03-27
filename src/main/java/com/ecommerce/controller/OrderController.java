package com.ecommerce.controller;

import com.ecommerce.dto.requestDto.OrderRequestDto;
import com.ecommerce.dto.responseDto.ApiResponseDto;
import com.ecommerce.dto.responseDto.OrderResponseDto;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDto request) {
        OrderResponseDto response = orderService.createOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Order created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<OrderResponseDto>>> getUserOrders(
            Authentication authentication) {
        List<OrderResponseDto> response = orderService.getUserOrders(authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Orders retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> getOrderById(
            Authentication authentication,
            @PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderById(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Order retrieved successfully", response));
    }
}

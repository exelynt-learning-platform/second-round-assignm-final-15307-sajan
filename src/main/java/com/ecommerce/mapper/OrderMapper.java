package com.ecommerce.mapper;

import com.ecommerce.dto.responseDto.OrderItemResponseDto;
import com.ecommerce.dto.responseDto.OrderResponseDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDto toResponseDto(Order order) {
        List<OrderItemResponseDto> items = order.getOrderItems().stream()
                .map(this::toOrderItemResponseDto)
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId())
                .items(items)
                .totalPrice(order.getTotalPrice())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingZipCode(order.getShippingZipCode())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .razorpayOrderId(order.getRazorpayOrderId())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem) {
        BigDecimal subtotal = orderItem.getPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        return OrderItemResponseDto.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .productImageUrl(orderItem.getProduct().getImageUrl())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(subtotal)
                .build();
    }
}

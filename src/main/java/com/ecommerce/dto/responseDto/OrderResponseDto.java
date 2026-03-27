package com.ecommerce.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String shippingCity;
    private String shippingZipCode;
    private String orderStatus;
    private String paymentStatus;
    private String razorpayOrderId;
    private LocalDateTime createdAt;
}

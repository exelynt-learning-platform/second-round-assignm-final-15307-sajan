package com.ecommerce.service;

import com.ecommerce.dto.requestDto.CartItemRequestDto;
import com.ecommerce.dto.requestDto.UpdateCartItemRequestDto;
import com.ecommerce.dto.responseDto.CartResponseDto;

public interface CartService {

    CartResponseDto getCart(String userEmail);

    CartResponseDto addItemToCart(String userEmail, CartItemRequestDto request);

    CartResponseDto updateCartItem(String userEmail, Long itemId, UpdateCartItemRequestDto request);

    CartResponseDto removeCartItem(String userEmail, Long itemId);

    void clearCart(String userEmail);
}

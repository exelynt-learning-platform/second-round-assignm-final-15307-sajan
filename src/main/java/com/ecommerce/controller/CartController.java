package com.ecommerce.controller;

import com.ecommerce.dto.requestDto.CartItemRequestDto;
import com.ecommerce.dto.requestDto.UpdateCartItemRequestDto;
import com.ecommerce.dto.responseDto.ApiResponseDto;
import com.ecommerce.dto.responseDto.CartResponseDto;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<CartResponseDto>> getCart(Authentication authentication) {
        CartResponseDto response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Cart retrieved successfully", response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponseDto<CartResponseDto>> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequestDto request) {
        CartResponseDto response = cartService.addItemToCart(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponseDto.success("Item added to cart successfully", response));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponseDto<CartResponseDto>> updateCartItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequestDto request) {
        CartResponseDto response = cartService.updateCartItem(authentication.getName(), itemId, request);
        return ResponseEntity.ok(ApiResponseDto.success("Cart item updated successfully", response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponseDto<CartResponseDto>> removeCartItem(
            Authentication authentication,
            @PathVariable Long itemId) {
        CartResponseDto response = cartService.removeCartItem(authentication.getName(), itemId);
        return ResponseEntity.ok(ApiResponseDto.success("Item removed from cart successfully", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponseDto<Void>> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Cart cleared successfully"));
    }
}

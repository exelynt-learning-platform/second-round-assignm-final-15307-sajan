package com.ecommerce.service;

import com.ecommerce.dto.requestDto.CartItemRequestDto;
import com.ecommerce.dto.requestDto.UpdateCartItemRequestDto;
import com.ecommerce.dto.responseDto.CartResponseDto;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.serviceImpl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;
    private CartResponseDto cartResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .cartItems(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(50)
                .build();

        cartResponse = CartResponseDto.builder()
                .id(1L)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .build();
    }

    @Test
    @DisplayName("Should get cart for user")
    void getCart_ShouldReturnCart() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toResponseDto(cart)).thenReturn(cartResponse);

        CartResponseDto result = cartService.getCart("john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should add item to cart")
    void addItemToCart_ShouldAddNewItem() {
        CartItemRequestDto request = CartItemRequestDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        when(cartMapper.toResponseDto(any(Cart.class))).thenReturn(cartResponse);

        CartResponseDto result = cartService.addItemToCart("john@example.com", request);

        assertThat(result).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should throw exception when stock is insufficient")
    void addItemToCart_ShouldThrowException_WhenInsufficientStock() {
        CartItemRequestDto request = CartItemRequestDto.builder()
                .productId(1L)
                .quantity(100)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItemToCart("john@example.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should remove item from cart successfully")
    void removeCartItem_ShouldRemoveItem() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartMapper.toResponseDto(any(Cart.class))).thenReturn(cartResponse);

        CartResponseDto result = cartService.removeCartItem("john@example.com", 1L);

        assertThat(result).isNotNull();
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("Should throw exception when cart item doesn't belong to user")
    void removeCartItem_ShouldThrowException_WhenNotOwned() {
        Cart otherCart = Cart.builder().id(99L).build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(otherCart)
                .product(product)
                .quantity(2)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThatThrownBy(() -> cartService.removeCartItem("john@example.com", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to your cart");
    }
}

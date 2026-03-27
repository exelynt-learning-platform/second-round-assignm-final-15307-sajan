package com.ecommerce.service;

import com.ecommerce.dto.requestDto.OrderRequestDto;
import com.ecommerce.dto.responseDto.OrderResponseDto;
import com.ecommerce.entity.*;
import com.ecommerce.enumeration.OrderStatus;
import com.ecommerce.enumeration.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.*;
import com.ecommerce.serviceImpl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Product product;
    private OrderRequestDto orderRequest;
    private OrderResponseDto orderResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(50)
                .build();

        CartItem cartItem = CartItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .cartItems(new ArrayList<>(List.of(cartItem)))
                .build();

        orderRequest = OrderRequestDto.builder()
                .shippingAddress("123 Main St")
                .shippingCity("Mumbai")
                .shippingZipCode("400001")
                .build();

        orderResponse = OrderResponseDto.builder()
                .id(1L)
                .totalPrice(new BigDecimal("199.98"))
                .shippingAddress("123 Main St")
                .shippingCity("Mumbai")
                .shippingZipCode("400001")
                .orderStatus("PENDING")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create order from cart successfully")
    void createOrder_ShouldReturnOrder_WhenCartNotEmpty() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(orderMapper.toResponseDto(any(Order.class))).thenReturn(orderResponse);

        OrderResponseDto result = orderService.createOrder("john@example.com", orderRequest);

        assertThat(result).isNotNull();
        assertThat(result.getShippingAddress()).isEqualTo("123 Main St");
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(productRepository).save(any(Product.class)); // Stock decremented
    }

    @Test
    @DisplayName("Should throw exception when creating order from empty cart")
    void createOrder_ShouldThrowException_WhenCartEmpty() {
        Cart emptyCart = Cart.builder()
                .id(1L)
                .user(user)
                .cartItems(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() -> orderService.createOrder("john@example.com", orderRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    @DisplayName("Should return user orders")
    void getUserOrders_ShouldReturnListOfOrders() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        when(orderMapper.toResponseDto(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponseDto> result = orderService.getUserOrders("john@example.com");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when order doesn't belong to user")
    void getOrderById_ShouldThrowException_WhenNotOwned() {
        User otherUser = User.builder().id(99L).build();
        Order order = Order.builder()
                .id(1L)
                .user(otherUser)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById("john@example.com", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to you");
    }
}

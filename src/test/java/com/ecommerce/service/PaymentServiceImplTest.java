package com.ecommerce.service;

import com.ecommerce.dto.responseDto.PaymentResponseDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.enumeration.OrderStatus;
import com.ecommerce.enumeration.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.serviceImpl.PaymentServiceImpl;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private RazorpayClient razorpayClient;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .build();

        order = Order.builder()
                .id(1L)
                .user(user)
                .totalPrice(new BigDecimal("199.98"))
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should throw exception when order doesn't belong to user")
    void createPaymentOrder_ShouldThrowException_WhenNotOwned() {
        User otherUser = User.builder().id(99L).build();
        Order otherOrder = Order.builder()
                .id(1L)
                .user(otherUser)
                .totalPrice(new BigDecimal("199.98"))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(otherOrder));

        assertThatThrownBy(() -> paymentService.createPaymentOrder("john@example.com", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to you");
    }

    @Test
    @DisplayName("Should throw exception when payment already completed")
    void createPaymentOrder_ShouldThrowException_WhenAlreadyPaid() {
        Order paidOrder = Order.builder()
                .id(1L)
                .user(user)
                .totalPrice(new BigDecimal("199.98"))
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() -> paymentService.createPaymentOrder("john@example.com", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Payment already completed");
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void createPaymentOrder_ShouldThrowException_WhenOrderNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentOrder("john@example.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.ecommerce.serviceImpl;

import com.ecommerce.dto.requestDto.PaymentVerificationRequestDto;
import com.ecommerce.dto.responseDto.PaymentResponseDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.enumeration.OrderStatus;
import com.ecommerce.enumeration.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.PaymentException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY = "INR";

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public PaymentResponseDto createPaymentOrder(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // verify order ownership
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Order does not belong to you");
        }

        // check if payment is already completed
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already completed for this order");
        }

        try {
            // amount in paise (smallest currency unit for INR)
            long amountInPaise = order.getTotalPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", CURRENCY);
            orderRequest.put("receipt", "order_receipt_" + order.getId());

            JSONObject notes = new JSONObject();
            notes.put("order_id", order.getId().toString());
            notes.put("user_email", userEmail);
            orderRequest.put("notes", notes);

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            // update order with razorpay order id
            order.setRazorpayOrderId(razorpayOrder.get("id"));
            orderRepository.save(order);

            return PaymentResponseDto.builder()
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(order.getTotalPrice())
                    .currency(CURRENCY)
                    .key(razorpayKeyId)
                    .build();

        } catch (RazorpayException e) {
            throw new PaymentException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String verifyPayment(PaymentVerificationRequestDto request) {
        try {
            // verify signature using razorpay sdk utility  ..
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (isValid) {
                // update order status ..
                Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Order", "razorpayOrderId", request.getRazorpayOrderId()));

                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.CONFIRMED);
                order.setRazorpayPaymentId(request.getRazorpayPaymentId());
                orderRepository.save(order);

                return "Payment verified successfully";
            } else {
                // mark payment as failed
                Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                        .orElse(null);
                if (order != null) {
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    orderRepository.save(order);
                }
                throw new PaymentException("Payment verification failed. Invalid signature.");
            }
        } catch (RazorpayException e) {
            throw new PaymentException("Payment verification error: " + e.getMessage(), e);
        }
    }
}

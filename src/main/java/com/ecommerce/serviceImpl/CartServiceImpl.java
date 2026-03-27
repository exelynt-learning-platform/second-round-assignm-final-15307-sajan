package com.ecommerce.serviceImpl;

import com.ecommerce.dto.requestDto.CartItemRequestDto;
import com.ecommerce.dto.requestDto.UpdateCartItemRequestDto;
import com.ecommerce.dto.responseDto.CartResponseDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCart(String userEmail) {
        Cart cart = getCartByUserEmail(userEmail);
        return cartMapper.toResponseDto(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addItemToCart(String userEmail, CartItemRequestDto request) {
        Cart cart = getCartByUserEmail(userEmail);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
        }

        // Refresh cart to get updated items
        Cart updatedCart = cartRepository.findByUserId(getUserByEmail(userEmail).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return cartMapper.toResponseDto(updatedCart);
    }

    @Override
    @Transactional
    public CartResponseDto updateCartItem(String userEmail, Long itemId, UpdateCartItemRequestDto request) {
        Cart cart = getCartByUserEmail(userEmail);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        // Verify cart ownership
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        if (cartItem.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + cartItem.getProduct().getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Cart updatedCart = cartRepository.findByUserId(getUserByEmail(userEmail).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return cartMapper.toResponseDto(updatedCart);
    }

    @Override
    @Transactional
    public CartResponseDto removeCartItem(String userEmail, Long itemId) {
        Cart cart = getCartByUserEmail(userEmail);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        // Verify cart ownership
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cartItemRepository.delete(cartItem);

        Cart updatedCart = cartRepository.findByUserId(getUserByEmail(userEmail).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return cartMapper.toResponseDto(updatedCart);
    }

    @Override
    @Transactional
    public void clearCart(String userEmail) {
        Cart cart = getCartByUserEmail(userEmail);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    private Cart getCartByUserEmail(String email) {
        User user = getUserByEmail(email);
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}

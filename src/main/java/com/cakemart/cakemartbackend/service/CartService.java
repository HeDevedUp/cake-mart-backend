package com.cakemart.cakemartbackend.service;

import com.cakemart.cakemartbackend.dto.CartDto;
import com.cakemart.cakemartbackend.dto.CartItemDto;
import com.cakemart.cakemartbackend.model.Cart;
import com.cakemart.cakemartbackend.model.CartItem;
import com.cakemart.cakemartbackend.model.Product;
import com.cakemart.cakemartbackend.model.User;
import com.cakemart.cakemartbackend.repository.CartItemRepository;
import com.cakemart.cakemartbackend.repository.CartRepository;
import com.cakemart.cakemartbackend.repository.ProductRepository;
import com.cakemart.cakemartbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartDto addToCart(String userEmail, Long productId, int quantity) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
        }

        User user = requireUser(userEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        return toDto(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public void removeFromCart(Long itemId) {
        if (itemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemId is required");
        }
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public CartDto updateQuantity(Long itemId, int quantity) {
        if (itemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemId is required");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        Cart cart = item.getCart();
        return toDto(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional(readOnly = true)
    public CartDto getUserCart(String userEmail) {
        requireUser(userEmail);
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
        return toDto(cart);
    }

    private User requireUser(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(item -> {
                    Product p = item.getProduct();
                    BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemDto(
                            item.getId(),
                            p.getId(),
                            p.getName(),
                            p.getPrice(),
                            p.getImageUrl(),
                            item.getQuantity(),
                            lineTotal
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemDto::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(
                cart.getId(),
                cart.getUser().getEmail(),
                items,
                total
        );
    }
}


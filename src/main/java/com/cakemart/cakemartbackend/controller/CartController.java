package com.cakemart.cakemartbackend.controller;

import com.cakemart.cakemartbackend.dto.AddToCartRequest;
import com.cakemart.cakemartbackend.dto.CartDto;
import com.cakemart.cakemartbackend.dto.UpdateCartItemRequest;
import com.cakemart.cakemartbackend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> add(@RequestBody AddToCartRequest request) {
        String email = currentUserEmail();
        return ResponseEntity.ok(cartService.addToCart(email, request.productId(), request.quantity()));
    }

    @PutMapping("/update")
    public ResponseEntity<CartDto> update(@RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(request.itemId(), request.quantity()));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Void> remove(@PathVariable Long itemId) {
        cartService.removeFromCart(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartDto> getMyCart() {
        return ResponseEntity.ok(cartService.getUserCart(currentUserEmail()));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}


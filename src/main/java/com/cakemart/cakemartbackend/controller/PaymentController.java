package com.cakemart.cakemartbackend.controller;

import com.cakemart.cakemartbackend.dto.CheckoutRequest;
import com.cakemart.cakemartbackend.dto.CheckoutResponse;
import com.cakemart.cakemartbackend.model.Order;
import com.cakemart.cakemartbackend.repository.OrderRepository;
import com.cakemart.cakemartbackend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    public PaymentController(PaymentService paymentService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest request) {
        String email = currentUserEmail();

        if (request == null || request.orderId() == null) {
            return ResponseEntity.badRequest().body(new CheckoutResponse(null));
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"
                ));

        // Basic ownership check: only the order owner can pay
        if (order.getUser() == null || order.getUser().getEmail() == null || !order.getUser().getEmail().equals(email)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden"
            );
        }

        String url = paymentService.createCheckoutSession(order);
        return ResponseEntity.ok(new CheckoutResponse(url));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}


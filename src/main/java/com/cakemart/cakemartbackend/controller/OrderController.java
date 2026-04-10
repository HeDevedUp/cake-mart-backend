package com.cakemart.cakemartbackend.controller;

import com.cakemart.cakemartbackend.dto.OrderDto;
import com.cakemart.cakemartbackend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDto> create() {
        return ResponseEntity.ok(orderService.createOrderFromCart(currentUserEmail()));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDto>> myOrders() {
        return ResponseEntity.ok(orderService.getUserOrders(currentUserEmail()));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}


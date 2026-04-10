package com.cakemart.cakemartbackend.dto;

import com.cakemart.cakemartbackend.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        String userEmail,
        double totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {}


package com.cakemart.cakemartbackend.dto;

public record OrderItemDto(
        Long id,
        Long productId,
        String productName,
        int quantity,
        double price
) {}


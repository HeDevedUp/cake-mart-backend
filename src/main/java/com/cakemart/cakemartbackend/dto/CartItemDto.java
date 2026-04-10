package com.cakemart.cakemartbackend.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Long id,
        Long productId,
        String productName,
        BigDecimal productPrice,
        String productImageUrl,
        int quantity,
        BigDecimal lineTotal
) {}


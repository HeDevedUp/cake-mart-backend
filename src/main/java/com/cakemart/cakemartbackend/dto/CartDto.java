package com.cakemart.cakemartbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        Long id,
        String userEmail,
        List<CartItemDto> items,
        BigDecimal totalAmount
) {}


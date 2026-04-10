package com.cakemart.cakemartbackend.repository;

import com.cakemart.cakemartbackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {}


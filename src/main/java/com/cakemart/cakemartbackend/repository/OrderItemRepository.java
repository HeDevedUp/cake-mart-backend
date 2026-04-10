package com.cakemart.cakemartbackend.repository;

import com.cakemart.cakemartbackend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}


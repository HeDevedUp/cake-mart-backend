package com.cakemart.cakemartbackend.repository;

import com.cakemart.cakemartbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}


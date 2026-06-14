package com.example.task_app.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_app.domain.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{    
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

package com.example.task_app.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;


import com.example.task_app.domain.enums.UserRole;
import com.example.task_app.domain.entity.User;
import com.example.task_app.domain.repository.UserRepository;
import com.example.task_app.dto.request.UserRegistrationRequest;
import com.example.task_app.dto.response.UserResponse;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private PasswordEncoder passwordEncoder;


    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("正常にユーザー登録ができる")
    void register_success(){
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");


        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        User savedUser = User.builder()
                        .id(1L)
                        .username("testuser")
                        .email("test@example.com")
                        .password("hashed_password")
                        .role(UserRole.USER)
                        .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("ユーザー名でUserDetailsが取得できる")
    void loadUserByUsername_success(){
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashed_password")
                .role(UserRole.USER)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        UserDetails result = userService.loadUserByUsername("testuser");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("hashed_password");

    }
}
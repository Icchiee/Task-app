package com.example.task_app.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.task_app.domain.enums.UserRole;
import com.example.task_app.domain.entity.User;
import com.example.task_app.domain.repository.UserRepository;
import com.example.task_app.dto.request.UserRegistrationRequest;
import com.example.task_app.dto.response.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserResponse register(UserRegistrationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("User already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("このメールアドレスは既に使われています");
        }
        User user = User.builder()
                    .username((request.getUsername()))
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(UserRole.USER)
                    .build();

        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: "+ username));
        return  org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}

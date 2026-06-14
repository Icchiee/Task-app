package com.example.task_app.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.task_app.domain.repository.TaskRepository;
import com.example.task_app.domain.repository.UserRepository;
import com.example.task_app.dto.request.TaskCreateRequest;

import lombok.RequiredArgsConstructor;

import com.example.task_app.domain.entity.Task;
import com.example.task_app.domain.entity.User;
import com.example.task_app.domain.enums.TaskStatus;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    public void create(TaskCreateRequest request, String username){
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: "+ username));

        Task task = Task.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .dueDate(request.getDueDate())
                    .user(user)
                    .status(TaskStatus.TODO)
                    .build();
        taskRepository.save(task);
    }
}

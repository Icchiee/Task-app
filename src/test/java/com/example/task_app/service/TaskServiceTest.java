// Skip because focused in implementing logic
package com.example.task_app.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.task_app.domain.entity.Task;
import com.example.task_app.domain.entity.User;
import com.example.task_app.domain.enums.UserRole;
import com.example.task_app.domain.repository.TaskRepository;
import com.example.task_app.domain.repository.UserRepository;
import com.example.task_app.dto.request.TaskCreateRequest;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("正常にタスクを作成できる")
    void create_success() {
        // 準備
        User mockUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role(UserRole.USER)
                .build();

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("テストタスク");
        request.setDescription("説明文");
        request.setDueDate(LocalDate.of(2026, 12, 31));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // 実行
        taskService.create(request, "testuser");

        // 検証
        verify(taskRepository).save(any(Task.c
    }

    @Test
    @DisplayName("存在しないユーザーでタスク作
    void create_throwsWhenUserNotFound() {
        // 準備
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("テストタスク");

        when(userRepository.findByUsername("unempty());

        // 実行・検証
        assertThrows(UsernameNotFoundException.class,
                () -> taskService.create(reque

        verify(taskRepository, never()).save(a
    }
}
package com.example.taskmanagement;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.TaskMapper;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.service.TaskService;
import com.example.taskmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private final UUID userId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    @Test
    void createTask_shouldSaveTask_forCurrentUser() {
        User user = User.builder().id(userId).username("john").build();
        TaskRequest request = TaskRequest.builder()
                .title("Test task")
                .description("Description")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.HIGH)
                .build();

        Task task = Task.builder()
                .id(taskId)
                .title("Test task")
                .description("Description")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.HIGH)
                .user(user)
                .build();

        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .title("Test task")
                .userId(userId)
                .build();

        when(userService.getCurrentUsername()).thenReturn("john");
        when(userService.getUserEntityByUsername("john")).thenReturn(user);
        when(taskMapper.toEntity(request, user)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.createTask(request);

        assertThat(result.getTitle()).isEqualTo("Test task");
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void getTaskById_shouldReturnTask_whenOwnedByUser() {
        User user = User.builder().id(userId).username("john").build();
        Task task = Task.builder().id(taskId).title("Task").user(user).build();
        TaskResponse response = TaskResponse.builder().id(taskId).title("Task").userId(userId).build();

        when(userService.getCurrentUsername()).thenReturn("john");
        when(userService.getUserEntityByUsername("john")).thenReturn(user);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.getTaskById(taskId);

        assertThat(result.getId()).isEqualTo(taskId);
    }

    @Test
    void getTaskById_shouldThrow_whenTaskNotFound() {
        User user = User.builder().id(userId).username("john").build();

        when(userService.getCurrentUsername()).thenReturn("john");
        when(userService.getUserEntityByUsername("john")).thenReturn(user);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found");
    }

    @Test
    void getAllTasks_shouldFilterByStatusAndPriority() {
        User user = User.builder().id(userId).username("john").build();
        Task task = Task.builder()
                .id(taskId)
                .title("Filtered")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.HIGH)
                .user(user)
                .build();

        TaskResponse response = TaskResponse.builder().id(taskId).title("Filtered").build();

        when(userService.getCurrentUsername()).thenReturn("john");
        when(userService.getUserEntityByUsername("john")).thenReturn(user);
        when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        List<TaskResponse> result = taskService.getAllTasks(TaskStatus.NEW, TaskPriority.HIGH);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Filtered");
    }

    @Test
    void deleteTask_shouldRemoveTask_whenOwnedByUser() {
        User user = User.builder().id(userId).username("john").build();
        Task task = Task.builder().id(taskId).user(user).build();

        when(userService.getCurrentUsername()).thenReturn("john");
        when(userService.getUserEntityByUsername("john")).thenReturn(user);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).delete(task);
    }
}
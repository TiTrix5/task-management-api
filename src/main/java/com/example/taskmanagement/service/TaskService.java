package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.TaskMapper;
import com.example.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(TaskStatus status, TaskPriority priority) {
        User currentUser = getCurrentUser();
        Specification<Task> spec = TaskSpecification.belongsToUser(currentUser.getId());

        if (status != null) {
            spec = spec.and(TaskSpecification.hasStatus(status));
        }
        if (priority != null) {
            spec = spec.and(TaskSpecification.hasPriority(priority));
        }

        return taskRepository.findAll(spec).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();
        Task task = taskMapper.toEntity(request, currentUser);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        taskMapper.updateEntity(request, task);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    private User getCurrentUser() {
        return userService.getUserEntityByUsername(userService.getCurrentUsername());
    }
}
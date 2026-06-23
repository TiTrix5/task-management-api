package com.example.taskmanagement.mapper;

import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
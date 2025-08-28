package com.aust.its.mapper;

import com.aust.its.dto.model.UserDto;
import com.aust.its.entity.User;

public class UserMapper {

    public static UserDto entityToDto(User user) {
        return UserDto
                .builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .build();
    }
}

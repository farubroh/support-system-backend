package com.aust.its.mapper;

import com.aust.its.dto.model.DeveloperDto;
import com.aust.its.entity.Developer;

public class DeveloperMapper {

    public static DeveloperDto entityToDto(Developer developer) {
        return DeveloperDto.builder()
                .id(developer.getId())
                .username(developer.getUser().getUsername()) // Assuming Developer has a `User` object
                .build();
    }
}

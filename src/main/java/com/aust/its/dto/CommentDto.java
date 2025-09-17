package com.aust.its.dto;

import com.aust.its.dto.model.UserDto;
import com.aust.its.dto.model.DeveloperDto;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CommentDto(
        long id,
        String comment,  // Renamed content to comment
        UserDto createdByDto,  // Information of the user who created the comment
        DeveloperDto developerDto,  // Information of the developer
        LocalDateTime createdAt
) { }

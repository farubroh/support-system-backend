package com.aust.its.dto.model;

import lombok.Builder;

@Builder
public record DeveloperDto(
        long id,
        String username,
        String role,        // Optional: Add more details
        String department   // Optional: Add more details
) { }

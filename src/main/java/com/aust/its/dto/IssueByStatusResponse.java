package com.aust.its.dto;

import com.aust.its.dto.CategoryDto;
import com.aust.its.entity.User;
import com.aust.its.enums.IssueStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class IssueByStatusResponse {
    private long id;
    private String title;
    private String description;
    private User user;
    private IssueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime rejectedAt;
    private String completedReason;
    private String rejectedReason;
    private String serialId;
    private String developerName;

    // New: make admin UI receive category data directly
    private String category;               // primary/first category name
    private List<CategoryDto> categories;
    private List<String> files;

}

package com.aust.its.dto;

import com.aust.its.dto.model.UserDto;
import com.aust.its.enums.IssueStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserIssueResponse {
    private long id;
    private String title;
    private String description;
    private IssueStatus status;
    private LocalDateTime createdAt;

    private String category;                  // primary category name
    private List<CategoryDto> categories;     // full list

    private UserDto user;                     // minimal user info
    private List<String> files;               // filenames saved for this issue
    private String completedReason;
    private String rejectionReason;
}

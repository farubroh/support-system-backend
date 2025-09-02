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

    /** Name of the relevant developer (assigned/resolved/rejected). */
    private String developerName;

    /** NEW: ID of the relevant developer for this status view. */
    private Long developerId;

    // Admin UI convenience
    private String category;               // primary category name
    private List<CategoryDto> categories;  // all categories
    private List<String> files;            // filenames
}

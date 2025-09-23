package com.aust.its.entity;

import com.aust.its.enums.IssueStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "issues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    private IssueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime rejectedAt;
    private String serialId; // it will assigned by admin when the issue is assigned to developer

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    @JsonBackReference
    @JsonIgnoreProperties({"assignedIssues", "resolvedIssues", "rejectedIssues"})
    private Developer assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolved_by_id")
    @JsonBackReference
    @JsonIgnoreProperties({"assignedIssues", "resolvedIssues", "rejectedIssues"})
    private Developer resolvedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rejected_by_id")
    @JsonBackReference
    @JsonIgnoreProperties({"assignedIssues", "resolvedIssues", "rejectedIssues"})
    private Developer rejectedBy;

    private String rejectedByAdmin;
    private String rejectionReason;
    private String completedReason;


    @ManyToMany
    @JoinTable(
            name = "issue_category",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;


    /** Primary/first category name as a flat string (used by both dashboards). */
    @JsonProperty("category")
    public String getPrimaryCategoryName() {
        if (categories == null || categories.isEmpty()) return null;
        return categories.get(0).getCategoryName();
    }

    /** Full list of category names (optional, if you ever want to show multiple tags). */
    @JsonProperty("categoryNames")
    public List<String> getAllCategoryNames() {
        if (categories == null || categories.isEmpty()) return List.of();
        return categories.stream().map(Category::getCategoryName).collect(Collectors.toList());
    }
}

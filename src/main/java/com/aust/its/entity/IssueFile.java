package com.aust.its.entity;

import jakarta.persistence.*;
import lombok.Builder;

@Builder
@Entity

public class IssueFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ Required
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}

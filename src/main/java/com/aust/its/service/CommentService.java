package com.aust.its.service;

import com.aust.its.dto.CommentDto;
import com.aust.its.entity.Comment;
import com.aust.its.entity.Developer;
import com.aust.its.entity.Issue;
import com.aust.its.entity.User;
import com.aust.its.repository.CommentRepository;
import com.aust.its.mapper.UserMapper;
import com.aust.its.mapper.DeveloperMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final UserService userService;
    private final DeveloperService developerService;

    public List<CommentDto> getCommentsByIssueId(Long issueId) {
        List<Comment> comments = commentRepository.findByIssueId(issueId);
        return comments.stream()
                .map(comment -> CommentDto.builder()
                        .id(comment.getId())
                        .comment(comment.getComment())  // Update content to comment
                        .createdByDto(UserMapper.entityToDto(comment.getCreatedBy()))  // User who created the comment
                        .developerDto(DeveloperMapper.entityToDto(comment.getDeveloper()))  // Developer associated
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public CommentDto saveComment(Long issueId, Long userId, Long developerId, String commentContent) {
        Issue issue = issueService.getIssueById(issueId);
        User user = userService.getById(userId);
        Developer developer = developerService.getById(developerId);

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setCreatedBy(user);
        comment.setDeveloper(developer);
        comment.setComment(commentContent);

        Comment savedComment = commentRepository.save(comment);

        return CommentDto.builder()
                .id(savedComment.getId())
                .comment(savedComment.getComment())  // Update content to comment
                .createdByDto(UserMapper.entityToDto(savedComment.getCreatedBy()))
                .developerDto(DeveloperMapper.entityToDto(savedComment.getDeveloper()))
                .createdAt(savedComment.getCreatedAt())
                .build();
    }
}

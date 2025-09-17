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
                        .content(comment.getContent())
                        .userDto(UserMapper.entityToDto(comment.getUser()))
                        .developerDto(DeveloperMapper.entityToDto(comment.getDeveloper()))
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public CommentDto saveComment(Long issueId, Long userId, Long developerId, String content) {
        // Fetch issue by ID using the newly added method in IssueService
        Issue issue = issueService.getIssueById(issueId);  // This will now work
        User user = userService.getById(userId);
        Developer developer = developerService.getById(developerId);

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setDeveloper(developer);
        comment.setContent(content);

        Comment savedComment = commentRepository.save(comment);

        return CommentDto.builder()
                .id(savedComment.getId())
                .content(savedComment.getContent())
                .userDto(UserMapper.entityToDto(savedComment.getUser()))
                .developerDto(DeveloperMapper.entityToDto(savedComment.getDeveloper()))
                .createdAt(savedComment.getCreatedAt())
                .build();
    }
}

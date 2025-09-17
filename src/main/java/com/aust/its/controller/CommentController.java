package com.aust.its.controller;

import com.aust.its.dto.CommentDto;
import com.aust.its.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/issue/{issueId}")
    public List<CommentDto> getCommentsByIssue(@PathVariable Long issueId) {
        return commentService.getCommentsByIssueId(issueId);
    }

    @PostMapping("/add")
    public CommentDto addComment(@RequestParam Long issueId, @RequestParam Long userId,
                                 @RequestParam Long developerId, @RequestParam String content) {
        return commentService.saveComment(issueId, userId, developerId, content);
    }
}

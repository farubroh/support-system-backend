package com.aust.its.controller;

import com.aust.its.annotation.swaggerapidoc.developercontroller.DeveloperIssueApiDoc;
import com.aust.its.annotation.swaggerapidoc.developercontroller.DeveloperListApiDoc;
import com.aust.its.dto.IssuesOfDeveloperDto;
import com.aust.its.entity.Developer;
import com.aust.its.service.DeveloperService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Developer APIs", description = "Developer actions related APIs")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private static final Logger logger = LoggerFactory.getLogger(DeveloperController.class);
    private final DeveloperService developerService;

    @DeveloperListApiDoc
    @GetMapping
    public ResponseEntity<List<Developer>> getDevelopers() {
        return ResponseEntity.ok(developerService.getAll());
    }


    @DeveloperIssueApiDoc
    @GetMapping("{id}/issues")
    public ResponseEntity<IssuesOfDeveloperDto> getIssuesOfDeveloper(@PathVariable("id") long userId) {
        Developer developer = developerService.getByUserId(userId);
        return ResponseEntity.ok(developerService.getIssuesOfDeveloper(developer.getId()));
    }
}

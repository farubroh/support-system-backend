package com.aust.its.controller;

import com.aust.its.dto.*;
import com.aust.its.dto.model.IssueDto;
import com.aust.its.dto.pagination.PageResponse;
import com.aust.its.entity.Issue;
import com.aust.its.enums.IssueStatus;
import com.aust.its.service.CategoryService;
import com.aust.its.service.IssueService;
import com.aust.its.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Tag(name = "Issue APIs", description = "Issue related APIs")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private static final Logger logger = LoggerFactory.getLogger(IssueController.class);
    private final IssueService issueService;
    private final UserService userService;
    private final CategoryService categoryService;

    @Operation(
            summary = "Get All Issues",
            description = "Retrieve all issues with pagination and optional status filter.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "status", description = "Optional issue status filter", example = "PENDING")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of issues retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping
    public ResponseEntity<PageResponse<IssueResponseDto>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) IssueStatus status) {
        return ResponseEntity.ok(issueService.getAllIssues(page, size, status));
    }


    @Operation(
            summary = "Submit New Issue",
            description = "Create a new issue.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Issue details",
                    content = @Content(schema = @Schema(implementation = IssuePayload.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = IssueDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PostMapping
    public ResponseEntity<IssueDto> submitIssue(@RequestBody IssuePayload issuePayload) {
        return ResponseEntity.ok(issueService.createIssue(issuePayload));
    }


    @Operation(
            summary = "Get Issues of a User",
            description = "Retrieve all issues assigned to a specific user filtered by status.",
            parameters = {
                    @Parameter(name = "id", description = "User ID", required = true, example = "user123"),
                    @Parameter(name = "status", description = "Issue status filter", required = true, example = "INPROGRESS")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of user's issues retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Issue.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Issue>> getIssues(@PathVariable("id") String userId,
                                                 @RequestParam IssueStatus status) {

        logger.info("finding issues of userId :: {} for status :: {}", userId, status);
        return ResponseEntity.ok(issueService.getIssuesByUserIdAndStatus(userId, status));
    }


    @Operation(
            summary = "Get Issues by Status",
            description = "Retrieve all issues filtered by status.",
            parameters = {
                    @Parameter(name = "status", description = "Issue status", required = true, example = "PENDING")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of issues retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IssueByStatusResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<List<IssueByStatusResponse>> getIssuesByStatus(@PathVariable("status") IssueStatus status) {
        logger.info("finding issues of status :: {}", status);
        return ResponseEntity.ok(issueService.getIssuesByStatus(status));
    }
    @Operation(
            summary = "Set Issue Category by Name",
            description = "Assign a category to an issue by category name.",
            parameters = {
                    @Parameter(name = "id", description = "Issue ID", required = true, example = "101"),
                    @Parameter(name = "categoryName", description = "Category name to be assigned", required = true, example = "Bug")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category assigned to issue successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Category not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PutMapping("{id}/category/by-name")
    public IssueDto setIssueCategoryByName(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("categoryName");
        if (name == null || name.isBlank()) throw new RuntimeException("categoryName required");
        return issueService.setIssueCategoryByName(id, name);
    }


    @Operation(
            summary = "Assign Issue to Developer",
            description = "Assign an issue to a developer.",
            parameters = {
                    @Parameter(name = "id", description = "Issue ID", required = true, example = "101")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Developer assignment details",
                    content = @Content(schema = @Schema(implementation = IssueAssignPayload.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue assigned successfully",
                            content = @Content(schema = @Schema(implementation = DeveloperAssignedResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PostMapping("{id}/assign")
    public ResponseEntity<DeveloperAssignedResponse> assignIssueToDeveloper(@PathVariable Long id,
                                                                            @RequestBody IssueAssignPayload issueAssignPayload) {
        logger.info("Assigning issue {} to developer", id);
        return ResponseEntity.ok(issueService.assignIssue(id, issueAssignPayload));
    }

    @Operation(
            summary = "Delete Issue",
            description = "Delete an issue by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "Issue ID", required = true, example = "101")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Issue deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Issue not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIssue(@PathVariable Long id) {
        try {
            issueService.deleteIssue(id);
            return ResponseEntity.ok(Map.of("message", "Issue deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @Operation(
            summary = "Create Issue with Files",
            description = "Create a new issue with attached files.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Issue details with files",
                    content = @Content(schema = @Schema(implementation = IssuePayload.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Issue created successfully with files"),
                    @ApiResponse(responseCode = "500", description = "Error saving issue")
            }
    )
    @PostMapping("/with-files")
    public ResponseEntity<?> createIssueWithFiles(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("userId") long userId,
            @RequestParam("categoryIds") List<Long> categoryIds,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            List<String> savedFileNames = new ArrayList<>();

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || originalFilename.isBlank()) continue;

                    String uploadDir = "D:/iums_images/" + userId;
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    File dest = new File(uploadDir, originalFilename);
                    file.transferTo(dest);

                    savedFileNames.add(originalFilename);
                }
            }

            IssueDto dto = issueService.createIssueWithFiles(
                    title, description, userId, categoryIds, savedFileNames
            );

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            logger.error("Error saving issue with files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving issue: " + e.getMessage());
        }
    }




    @Operation(
            summary = "Reject Issue",
            description = "Reject an issue with reason.",
            parameters = {
                    @Parameter(name = "id", description = "Issue ID", required = true, example = "101")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Issue rejection details",
                    content = @Content(schema = @Schema(implementation = IssueRejectPayload.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue rejected successfully",
                            content = @Content(schema = @Schema(implementation = IssueRejectResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PostMapping("{id}/reject")
    public ResponseEntity<IssueRejectResponse> rejectIssue(@PathVariable Long id,
                                                           @RequestBody IssueRejectPayload issueRejectPayload) {
        return ResponseEntity.ok(issueService.rejectIssue(id, issueRejectPayload));
    }


    @Operation(
            summary = "Update Issue Status",
            description = "Update the status of an issue.",
            parameters = {
                    @Parameter(name = "id", description = "Issue ID", required = true, example = "101")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Status update payload",
                    content = @Content(schema = @Schema(implementation = IssueStatusUpdatePayload.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue status updated successfully",
                            content = @Content(schema = @Schema(implementation = IssueDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PutMapping("{id}/status")
    public ResponseEntity<IssueDto> updateStatus(@PathVariable Long id,
                                                 @RequestBody IssueStatusUpdatePayload issueStatusUpdatePayload) {
        return ResponseEntity.ok(issueService.updateIssueByStatus(id, issueStatusUpdatePayload));
    }


    @Operation(
            summary = "Update Issue Assignee",
            description = "Change the developer assigned to an issue.",
            parameters = {
                    @Parameter(name = "id", description = "Issue ID", required = true, example = "101")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "New assignee details",
                    content = @Content(schema = @Schema(implementation = AssignDeveloperPayload.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue assignee updated successfully",
                            content = @Content(schema = @Schema(implementation = IssueDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PutMapping("{id}/assign")
    public ResponseEntity<IssueDto> updateAssignee(@PathVariable("id") Long issueId,
                                                   @RequestBody AssignDeveloperPayload assignDeveloperPayload) {
        return ResponseEntity.ok(issueService.updateAssignee(issueId, assignDeveloperPayload.developerId()));
    }

    @Operation(
            summary = "Get Issue Count",
            description = "Retrieve the count of all issues grouped by criteria.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue counts retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IssueCountDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping("/count")
    public ResponseEntity<List<IssueCountDto>> getIssueCount() {
        return ResponseEntity.ok(issueService.getAllIssueCount());
    }


    @Operation(
            summary = "Get Issue Count by Status",
            description = "Retrieve the count of issues for a specific status.",
            parameters = {
                    @Parameter(name = "status", description = "Issue status", required = true, example = "PENDING")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Issue count retrieved successfully",
                            content = @Content(schema = @Schema(implementation = IssueCountDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{status}/count")
    public ResponseEntity<IssueCountDto> getIssueCountByStatus(@PathVariable("status") IssueStatus issueStatus) {
        return ResponseEntity.ok(issueService.getIssueCountByStatus(issueStatus));
    }


    //new controller for file:
    @PostMapping("/with-files")
    public ResponseEntity<?> createIssueWithFiles(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("userId") Long userId,
            @RequestParam("categoryIds") List<Long> categoryIds, // Fix here: Use categoryIds instead of category
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            List<String> savedFileNames = new ArrayList<>();

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || originalFilename.isBlank()) continue;

                    String uploadDir = "D:/iums_images/" + userId;
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File dest = new File(uploadDir + "/" + originalFilename);
                    file.transferTo(dest);

                    savedFileNames.add(originalFilename);
                }
            }

            // Call the service method with the correct categoryIds
            IssueDto newIssue = issueService.createIssueWithFiles(
                    title, description, userId, categoryIds, savedFileNames // Corrected here
            );

            return ResponseEntity.ok(newIssue);

        } catch (Exception e) {
            logger.error("file exception {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving issue: " + e.getMessage());
        }
    }


    @Operation(
            summary = "Get File",
            description = "Retrieve a specific file by user ID and filename.",
            parameters = {
                    @Parameter(name = "userId", description = "User ID", required = true, example = "user123"),
                    @Parameter(name = "filename", description = "File name", required = true, example = "file1.png")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "File retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "File not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/files/{userId}/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String userId, @PathVariable String filename) {
        try {
            Path path = Paths.get("D:\\Austi" + userId).resolve(filename);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
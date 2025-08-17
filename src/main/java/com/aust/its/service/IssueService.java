package com.aust.its.service;

import com.aust.its.dto.*;
import com.aust.its.dto.model.IssueDto;
import com.aust.its.dto.model.UserDto;
import com.aust.its.entity.*;
import com.aust.its.enums.IssueStatus;
import com.aust.its.enums.Role;
import com.aust.its.mapper.CategoryMapper;
import com.aust.its.mapper.IssueMapper;
import com.aust.its.mapper.UserMapper;
import com.aust.its.repository.IssueFileRepository;
import com.aust.its.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);
    private static final String FILE_SAVE_DIRECTORY = "D:/iums_images";
    private final IssueFileRepository issueFileRepository;

    private final IssueRepository issueRepository;
    private final UserService userService;
    private final DeveloperService developerService;

    public List<Issue> getIssuesByUserIdAndStatus(Long userId, IssueStatus status) {
        List<Issue> issues = issueRepository.findByUserIdAndStatus(userId, status);
        logger.info("issues by userId and status : {}", issues);
        return issues;
    }

    public List<IssueByStatusResponse> getIssuesByStatus(IssueStatus status) {
        List<Issue> issues = issueRepository.findByStatus(status);

        List<IssueByStatusResponse> responses = new ArrayList<>();

        for (Issue issue : issues) {
            IssueByStatusResponse response =
                    IssueByStatusResponse
                            .builder()
                            .id(issue.getId())
                            .title(issue.getTitle())
                            .description(issue.getDescription())
                            .user(issue.getUser())
                            .status(issue.getStatus())
                            .createdAt(issue.getCreatedAt())
                            .completedAt(issue.getCompletedAt())
                            .serialId(issue.getSerialId())
                            .build();

            if(IssueStatus.PENDING.equals(issue.getStatus()) || IssueStatus.INPROGRESS.equals(issue.getStatus())) {
                if(issue.getAssignedTo() != null) {
                    response.setDeveloperName(issue.getAssignedTo().getUser().getUsername());
                }
            }
            if(IssueStatus.COMPLETED.equals(issue.getStatus())) {
                if(issue.getResolvedBy() != null) {
                    response.setDeveloperName(issue.getResolvedBy().getUser().getUsername());
                    response.setCompletedReason(issue.getCompletedReason());
                    response.setCompletedAt(issue.getCompletedAt());
                }
            }
            if(IssueStatus.REJECTED.equals(issue.getStatus())) {
                if(issue.getRejectedBy() != null) {
                    response.setDeveloperName(issue.getRejectedBy().getUser().getUsername());
                    response.setRejectedReason(issue.getRejectionReason());
                    response.setRejectedAt(issue.getRejectedAt());
                }
                else {
                    response.setDeveloperName(issue.getRejectedByAdmin());
                    response.setRejectedReason(issue.getRejectionReason());
                    response.setRejectedAt(issue.getRejectedAt());
                }
            }

            responses.add(response);
        }

        logger.info("issues by status : {}", issues);
        return responses;
    }

    public DeveloperAssignedResponse assignIssue(Long issueId, final IssueAssignPayload issueAssignPayload) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));

        Developer developer = developerService.getById(issueAssignPayload.developerId());
        issue.setAssignedTo(developer);

        List<Issue> assignedIssues = developer.getAssignedIssues();
        assignedIssues.add(issue);

        developer.setAssignedIssues(assignedIssues);

        issueRepository.save(issue);
        developerService.save(developer);

        return DeveloperAssignedResponse
                .builder()
                .developerName(developer.getUser().getUsername())
                .currentlyTotalTaskInHand(developer.getAssignedIssues().size())
                .build();
    }

    public IssueRejectResponse rejectIssue(final Long issueId, final IssueRejectPayload issueRejectPayload) {
        User user = new User();

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));

        issue.setStatus(IssueStatus.REJECTED);
        issue.setRejectedAt(LocalDateTime.now());
        issue.setRejectionReason(issueRejectPayload.rejectionReason());

        if(Role.ADMIN.getName().equalsIgnoreCase(issueRejectPayload.rejectedByRole())) {
            user = userService.getById(issueRejectPayload.rejectedById());
            issue.setRejectedByAdmin(user.getUsername());
        }
        else if(Role.DEVELOPER.getName().equalsIgnoreCase(issueRejectPayload.rejectedByRole())) {
            Developer developer = developerService.getById(issueRejectPayload.rejectedById());
            user = developer.getUser();
            issue.setRejectedBy(developer);
        }

        Issue savedIssue = issueRepository.save(issue);
        logger.info("saved issue : {}", savedIssue);

        return IssueRejectResponse
                .builder()
                .rejectedByName(user.getUsername())
                .rejectedByRole(user.getRole())
                .status(savedIssue.getStatus())
                .build();
    }

    public Issue updateIssueByStatus(Long issueId, IssueStatusUpdatePayload issueStatusUpdatePayload) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));

        User user = userService.getById(issueStatusUpdatePayload.workedBy());
        Developer developer = developerService.getByUserId(user.getId());

        if(IssueStatus.PENDING.equals(issueStatusUpdatePayload.fromStatus()) ||
                IssueStatus.INPROGRESS.equals(issueStatusUpdatePayload.fromStatus())) {

            if(IssueStatus.PENDING.equals(issueStatusUpdatePayload.toStatus()) ||
                    IssueStatus.INPROGRESS.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setAssignedTo(developer);
            }
            else if(IssueStatus.COMPLETED.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setAssignedTo(null);
                issue.setResolvedBy(developer);
                issue.setCompletedAt(LocalDateTime.now());
                issue.setCompletedReason(issueStatusUpdatePayload.completedAnalysis());
            }
            else if(IssueStatus.REJECTED.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setAssignedTo(null);
                issue.setRejectedBy(developer);
                issue.setRejectedAt(LocalDateTime.now());
                issue.setRejectionReason(issueStatusUpdatePayload.rejectionReason());
            }
        }
        else if(IssueStatus.COMPLETED.equals(issueStatusUpdatePayload.fromStatus())) {
            if(IssueStatus.PENDING.equals(issueStatusUpdatePayload.toStatus()) ||
                    IssueStatus.INPROGRESS.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setAssignedTo(developer);
                issue.setResolvedBy(null);
            }
            else if(IssueStatus.COMPLETED.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setAssignedTo(null);
                issue.setResolvedBy(developer);
                issue.setCompletedAt(LocalDateTime.now());
                issue.setCompletedReason(issueStatusUpdatePayload.completedAnalysis());
            }
            else if(IssueStatus.REJECTED.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setResolvedBy(null);
                issue.setRejectedBy(developer);
                issue.setRejectedAt(LocalDateTime.now());
                issue.setRejectionReason(issueStatusUpdatePayload.rejectionReason());
            }
        }
        else if(IssueStatus.REJECTED.equals(issueStatusUpdatePayload.fromStatus())) {
            if(IssueStatus.PENDING.equals(issueStatusUpdatePayload.toStatus()) ||
                    IssueStatus.INPROGRESS.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setAssignedTo(developer);
                issue.setRejectedBy(null);
            }
            else if(IssueStatus.COMPLETED.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setResolvedBy(developer);
                issue.setRejectedBy(null);
                issue.setCompletedAt(LocalDateTime.now());
                issue.setCompletedReason(issueStatusUpdatePayload.completedAnalysis());
            }
            else if(IssueStatus.REJECTED.equals(issueStatusUpdatePayload.toStatus())) {
                issue.setStatus(issueStatusUpdatePayload.toStatus());
                issue.setRejectedBy(developer);
                issue.setRejectedAt(LocalDateTime.now());
                issue.setRejectionReason(issueStatusUpdatePayload.rejectionReason());
            }
        }

        return issueRepository.save(issue);
    }

    public Issue updateAssignee(Long issueId, Long developerId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));

        Developer developer = developerService.getById(developerId);

        if(IssueStatus.PENDING.equals(issue.getStatus()) ||
                IssueStatus.INPROGRESS.equals(issue.getStatus())) {
            issue.setAssignedTo(developer);
        }
        else if(IssueStatus.COMPLETED.equals(issue.getStatus())) {
            issue.setResolvedBy(developer);
        }
        else if(IssueStatus.REJECTED.equals(issue.getStatus())) {
            issue.setRejectedBy(developer);
        }

        return issueRepository.save(issue);
    }

    public IssueDto getIssueResponse(Issue issuePayload) {
        Issue issue = issueRepository.save(issuePayload);
        User user = issue.getUser();
        List<Category> categories = issue.getCategories();
        return IssueMapper.entityToDto(issue, user, categories);
    }

    public List<IssueCountDto> getAllIssueCount() {
        List<Issue> issues = issueRepository.findAll();
        Map<IssueStatus, Long> issueCountMap = new HashMap<>();

        for (Issue issue : issues) {
            issueCountMap.merge(issue.getStatus(), 1L, Long::sum);
        }

        return issueCountMap.entrySet()
                .stream()
                .map(e -> new IssueCountDto(e.getKey(), e.getValue()))
                .toList();
    }

    public IssueCountDto getIssueCountByStatus(IssueStatus issueStatus) {
        List<Issue> issues = issueRepository.findAll();
        long count = issues.stream().filter(i -> i.getStatus().equals(issueStatus)).count();
        return new IssueCountDto(issueStatus, count);
    }


    //code for the file
    public Issue createIssueWithFiles(String title, String description, Long userId, String category, List<String> uploadedFilenames) {
        // Get user
        User user = userService.getById(userId);

        // Create issue and save
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setUser(user);
        issue.setStatus(IssueStatus.PENDING);


        issue.setCreatedAt(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue); // Save to get issue ID

        // Map each filename to an IssueFile entity
        List<IssueFile> issueFiles = uploadedFilenames.stream()
                .map(fileName -> IssueFile.builder()
                        .fileName(fileName)
                        .issue(savedIssue)
                        .user(user)
                        .build())
                .collect(Collectors.toList());

        // Save file metadata to DB
        issueFileRepository.saveAll(issueFiles);

        return savedIssue;
    }

    // Utility method (optional): Save actual file to D:/iums_images
    public String saveFileToDisk(String userId, String originalFilename, byte[] fileBytes) throws Exception {
        String dirPath = FILE_SAVE_DIRECTORY + "/" + userId;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File savedFile = new File(dir, originalFilename);
        java.nio.file.Files.write(savedFile.toPath(), fileBytes);
        return originalFilename; // Return saved file name
    }
}

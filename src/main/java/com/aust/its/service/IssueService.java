package com.aust.its.service;

import com.aust.its.dto.*;
import com.aust.its.dto.model.IssueDto;
import com.aust.its.dto.model.UserDto;
import com.aust.its.dto.pagination.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String FILE_SAVE_DIRECTORY = "C:/Users/austi/Downloads/iums_images";
    private final IssueFileRepository issueFileRepository;
    private final IssueRepository issueRepository;
    private final UserService userService;
    private final DeveloperService developerService;
    private final CategoryService categoryService;

    public List<Issue> getIssuesByUserIdAndStatus(String userId, IssueStatus status) {
        return issueRepository.findByUserIdAndStatus(userId, status);
    }

    public List<IssueByStatusResponse> getIssuesByStatus(IssueStatus status) {
        List<Issue> issues = issueRepository.findByStatus(status);
        List<IssueByStatusResponse> responses = new ArrayList<>();

        for (Issue issue : issues) {
            try {
                IssueByStatusResponse response = IssueMapper.getIssueByStatus(issue);

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
                    String rejectedBy = issue.getRejectedBy() != null ? issue.getRejectedBy().getUser().getUsername() : issue.getRejectedByAdmin();
                    response.setDeveloperName(rejectedBy);
                    response.setRejectedReason(issue.getRejectionReason());
                    response.setRejectedAt(issue.getRejectedAt());
                }

                responses.add(response);
            } catch (Exception e) {
                logger.error("getIssuesByStatus error :: {}", e.getMessage(), e);
            }
        }

        return responses;
    }
    @Transactional(readOnly = true)
    public List<UserIssueResponse> getUserIssuesWithStatus(String userId, IssueStatus status) {
        List<Issue> issues = issueRepository.findByUserIdAndStatus(userId, status);

        return issues.stream().map(issue -> {
            var categoryDtos = (issue.getCategories() == null)
                    ? List.<CategoryDto>of()
                    : issue.getCategories().stream().map(CategoryMapper::entityToDto).toList();
            String primaryCategory = categoryDtos.isEmpty() ? null : categoryDtos.get(0).categoryName();

            List<String> files = issueFileRepository.findByIssueId(issue.getId())
                    .stream()
                    .map(IssueFile::getFileName)
                    .collect(Collectors.toList());

            UserDto userDto = UserMapper.entityToDto(issue.getUser());

            return UserIssueResponse.builder()
                    .id(issue.getId())
                    .title(issue.getTitle())
                    .description(issue.getDescription())
                    .status(issue.getStatus())
                    .createdAt(issue.getCreatedAt())
                    .category(primaryCategory)
                    .categories(categoryDtos)
                    .user(userDto)
                    .files(files)
                    .completedReason(issue.getCompletedReason())
                    .rejectionReason(issue.getRejectionReason())
                    .build();
        }).toList();
    }

    @Transactional
    public DeveloperAssignedResponse assignIssue(Long issueId, final IssueAssignPayload issueAssignPayload) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("issue not found with issue id : " + issueId));

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

    @Transactional
    public IssueRejectResponse rejectIssue(final Long issueId, final IssueRejectPayload issueRejectPayload) {
        User user = new User();

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("issue not found with issue id : " + issueId));

        issue.setStatus(IssueStatus.REJECTED);
        issue.setRejectedAt(LocalDateTime.now());
        issue.setRejectionReason(issueRejectPayload.rejectionReason());

        if(Role.ADMIN.getName().equalsIgnoreCase(issueRejectPayload.rejectedByRole())) {
            user = userService.getById(String.valueOf(issueRejectPayload.rejectedById()));
            issue.setRejectedByAdmin(user.getUsername());
        }
        else if(Role.DEVELOPER.getName().equalsIgnoreCase(issueRejectPayload.rejectedByRole())) {
            Developer developer = developerService.getById(issueRejectPayload.rejectedById());
            user = developer.getUser();
            issue.setRejectedBy(developer);
        }

        Issue savedIssue = issueRepository.save(issue);

        return IssueRejectResponse
                .builder()
                .rejectedByName(user.getUsername())
                .rejectedByRole(user.getRole())
                .status(savedIssue.getStatus())
                .build();
    }

    public IssueDto updateIssueByStatus(Long issueId, IssueStatusUpdatePayload issueStatusUpdatePayload) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("issue not found with issue id : " + issueId));

        User user = userService.getById(issueStatusUpdatePayload.workedBy());
        Developer developer = developerService.getByUserId(Long.parseLong(user.getId()));

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

        Issue savedIssue = issueRepository.save(issue);
        return IssueMapper.entityToDto(savedIssue);
    }

    public IssueDto updateAssignee(Long issueId, Long developerId) {
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

        Issue savedIssue = issueRepository.save(issue);
        return IssueMapper.entityToDto(savedIssue);
    }

    @Transactional
    public IssueDto createIssue(IssuePayload issuePayload) {
        User user = userService.getById(issuePayload.userId());
        List<Category> categories = categoryService.getCategoriesByCategoryIdList(issuePayload.categoryIds());
        Issue issue = IssueMapper.payloadToEntity(issuePayload, user, categories);
        return getIssueResponse(issue);
    }

    private IssueDto getIssueResponse(Issue issuePayload) {
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
    public IssueDto createIssueWithFiles(
            String title,
            String description,
            Long userId,
            List<Long> categoryIds, // This parameter is expected
            List<String> uploadedFilenames
    ) {
        User user = userService.getById(String.valueOf(userId));
        List<Category> categories = categoryService.getCategoriesByCategoryIdList(categoryIds); // Here we fetch categories by IDs

        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setUser(user);
        issue.setStatus(IssueStatus.PENDING);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setCategories(categories);

        Issue savedIssue = issueRepository.save(issue);

        if (uploadedFilenames != null && !uploadedFilenames.isEmpty()) {
            List<IssueFile> issueFiles = uploadedFilenames.stream()
                    .map(fileName -> IssueFile.builder()
                            .fileName(fileName)
                            .issue(savedIssue)
                            .user(user)
                            .build())
                    .collect(Collectors.toList());
            issueFileRepository.saveAll(issueFiles);
        }

        return IssueMapper.entityToDto(savedIssue, user, categories);
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


    public PageResponse<IssueResponseDto> getAllIssues(int page, int size, IssueStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Issue> issuePage;

        if (status != null) {
            issuePage = issueRepository.findByStatus(status, pageable);
        } else {
            issuePage = issueRepository.findAll(pageable);
        }

        List<IssueResponseDto> issueList = issuePage.getContent().stream().map(issue ->
                IssueResponseDto.builder()
                        .id(issue.getId())
                        .title(issue.getTitle())
                        .description(issue.getDescription())
                        .username(issue.getUser().getUsername())
                        .status(issue.getStatus().name())
                        .createdAt(issue.getCreatedAt())
                        .completedAt(issue.getCompletedAt())
                        .rejectedAt(issue.getRejectedAt())
                        .serialId(issue.getSerialId())
                        .assignedTo(issue.getAssignedTo() != null ? issue.getAssignedTo().getUser().getUsername() : null)
                        .resolvedBy(issue.getResolvedBy() != null ? issue.getResolvedBy().getUser().getUsername() : null)
                        .rejectedBy(issue.getRejectedBy() != null ? issue.getRejectedBy().getUser().getUsername() : null)
                        .categories(CategoryMapper.entityListToDtoList(issue.getCategories()))
                        .build()
        ).toList();

        return PageResponse.<IssueResponseDto>builder()
                .content(issueList)
                .pageNumber(issuePage.getNumber())
                .pageSize(issuePage.getSize())
                .totalElements(issuePage.getTotalElements())
                .totalPages(issuePage.getTotalPages())
                .last(issuePage.isLast())
                .build();
    }

    public IssueDto setIssueCategoryByName(Long issueId, String categoryName) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));
        Category category = categoryService.getOrCreateByName(categoryName);
        issue.setCategories(List.of(category)); // replace with single selection; make it List.of(...)
        Issue saved = issueRepository.save(issue);
        return IssueMapper.entityToDto(saved, saved.getUser(), saved.getCategories());
    }
    public void deleteIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));
        issueRepository.delete(issue);
    }
    public Issue getIssueById(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + issueId));
    }
}

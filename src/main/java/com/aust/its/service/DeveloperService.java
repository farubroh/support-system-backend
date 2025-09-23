package com.aust.its.service;

import com.aust.its.annotation.IsAuthorizedPerson;
import com.aust.its.dto.IssuesOfDeveloperDto;
import com.aust.its.dto.model.DeveloperSpecificIssueDto;
import com.aust.its.dto.model.IssueDto;
import com.aust.its.dto.model.UserDto;
import com.aust.its.entity.Developer;
import com.aust.its.entity.Issue;
import com.aust.its.entity.User;
import com.aust.its.enums.IssueStatus;
import com.aust.its.repository.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private static final Logger logger = LoggerFactory.getLogger(DeveloperService.class);

    private final DeveloperRepository developerRepository;
    private final UserService userService;

    @IsAuthorizedPerson
    public List<Developer> getAll() {
        return developerRepository.findAll();
    }

    public Developer getById(long id) {
        return developerRepository.findByIdWithAssignedIssues(id).orElse(null);
    }

    public Developer save(Developer developer) {
        logger.info("Saving developer: {}", developer);
        return developerRepository.save(developer);
    }

    public IssuesOfDeveloperDto getIssuesOfDeveloper(Long developerId) {
        Developer developer = this.getById(developerId);
        List<Issue> assignedIssues = developer.getAssignedIssues();
        List<Issue> resolvedIssues = developer.getResolvedIssues();
        List<Issue> rejectedIssues = developer.getRejectedIssues();

        List<DeveloperSpecificIssueDto> pendingIssueList = new ArrayList<>();
        List<DeveloperSpecificIssueDto> ongoingIssueList = new ArrayList<>();
        List<DeveloperSpecificIssueDto> completedIssueList = new ArrayList<>();
        List<DeveloperSpecificIssueDto> rejectedIssueList = new ArrayList<>();

        for(Issue issue : assignedIssues) {
            if(IssueStatus.PENDING.equals(issue.getStatus())) {
                User issuer = issue.getUser();

                UserDto userDto =
                        UserDto
                                .builder()
                                .username(issuer.getUsername())
                                .department(issuer.getDepartment())
                                .role(issuer.getRole())
                                .designation(issuer.getDesignation())
                                .build();

                IssueDto issueDto =
                        IssueDto
                                .builder()
                                .issueId(issue.getId())
                                .status(issue.getStatus())
                                .title(issue.getTitle())
                                .description(issue.getDescription())
                                .serialId(issue.getSerialId())
                                .createdAt(issue.getCreatedAt())
                                .build();

                DeveloperSpecificIssueDto developerSpecificIssueDto =
                        DeveloperSpecificIssueDto
                                .builder()
                                .createdBy(userDto)
                                .issue(issueDto)
                                .build();

                pendingIssueList.add(developerSpecificIssueDto);
            }
            else if(IssueStatus.INPROGRESS.equals(issue.getStatus())) {
                User issuer = issue.getUser();

                UserDto userDto =
                        UserDto
                                .builder()
                                .username(issuer.getUsername())
                                .department(issuer.getDepartment())
                                .role(issuer.getRole())
                                .designation(issuer.getDesignation())
                                .build();

                IssueDto issueDto =
                        IssueDto
                                .builder()
                                .issueId(issue.getId())
                                .status(issue.getStatus())
                                .title(issue.getTitle())
                                .description(issue.getDescription())
                                .serialId(issue.getSerialId())
                                .createdAt(issue.getCreatedAt())
                                .build();

                DeveloperSpecificIssueDto developerSpecificIssueDto =
                        DeveloperSpecificIssueDto
                                .builder()
                                .createdBy(userDto)
                                .issue(issueDto)
                                .build();

                ongoingIssueList.add(developerSpecificIssueDto);
            }
        }

        for(Issue issue : resolvedIssues) {
            User issuer = issue.getUser();

            UserDto userDto =
                    UserDto
                            .builder()
                            .username(issuer.getUsername())
                            .department(issuer.getDepartment())
                            .role(issuer.getRole())
                            .designation(issuer.getDesignation())
                            .build();

            IssueDto issueDto =
                    IssueDto
                            .builder()
                            .issueId(issue.getId())
                            .status(issue.getStatus())
                            .title(issue.getTitle())
                            .description(issue.getDescription())
                            .serialId(issue.getSerialId())
                            .createdAt(issue.getCreatedAt())
                            .completedAt(issue.getCompletedAt())
                            .completedReason(issue.getCompletedReason())
                            .build();

            DeveloperSpecificIssueDto developerSpecificIssueDto =
                    DeveloperSpecificIssueDto
                            .builder()
                            .createdBy(userDto)
                            .issue(issueDto)
                            .build();

            completedIssueList.add(developerSpecificIssueDto);
        }

        for(Issue issue : rejectedIssues) {
            User issuer = issue.getUser();

            UserDto userDto =
                    UserDto
                            .builder()
                            .username(issuer.getUsername())
                            .department(issuer.getDepartment())
                            .role(issuer.getRole())
                            .designation(issuer.getDesignation())
                            .build();

            IssueDto issueDto =
                    IssueDto
                            .builder()
                            .issueId(issue.getId())
                            .status(issue.getStatus())
                            .title(issue.getTitle())
                            .description(issue.getDescription())
                            .serialId(issue.getSerialId())
                            .createdAt(issue.getCreatedAt())
                            .rejectedAt(issue.getRejectedAt())
                            .rejectionReason(issue.getRejectionReason())
                            .build();

            DeveloperSpecificIssueDto developerSpecificIssueDto =
                    DeveloperSpecificIssueDto
                            .builder()
                            .createdBy(userDto)
                            .issue(issueDto)
                            .build();

            rejectedIssueList.add(developerSpecificIssueDto);
        }

        return IssuesOfDeveloperDto
                .builder()
                .pendingIssues(pendingIssueList)
                .ongoingIssues(ongoingIssueList)
                .completedIssues(completedIssueList)
                .rejectedIssues(rejectedIssueList)
                .build();
    }

    public Developer getByUserId(long userId) {
        User user = userService.getById(String.valueOf(userId));
        return developerRepository.findByUser(user);
    }
}

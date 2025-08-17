package com.aust.its.repository;

import com.aust.its.entity.IssueFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IssueFileRepository extends JpaRepository<IssueFile, Long> {
    List<IssueFile> findByIssueId(Long issueId);
}
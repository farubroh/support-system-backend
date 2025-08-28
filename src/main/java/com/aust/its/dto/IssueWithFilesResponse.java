package com.aust.its.dto;

import com.aust.its.dto.model.IssueDto;
import java.util.List;

public record IssueWithFilesResponse(
        IssueDto issue,
        List<String> files
) {}
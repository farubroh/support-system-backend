package com.aust.its.dto;

import java.util.List;

public record IumsUserResponse(String userId,
                               String name,
                               String personalEmail,
                               String eduEmail,
                               String deptShortName,
                               String deptLongName,
                               Boolean isStudent,
                               String programShortName,
                               String programLongName,
                               List<IumsRoleInfo> roleInfo) {
}

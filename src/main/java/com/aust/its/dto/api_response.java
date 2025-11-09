package com.aust.its.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record api_response(
        String deptLongName,
        String deptShortName,
        Integer designationId,
        String designationName,
        String eduEmail,
        boolean isStudent,
        String name,
        String personalEmail,
        String programLongName,
        String programShortName,
        List<RoleInfo> roleInfo,
        String userId
) {

    public record RoleInfo(
            Integer roleId,
            String roleName
    ) {}
}

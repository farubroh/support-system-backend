package com.aust.its.dto;

import java.util.List;

public record UpdateIssueCategoriesPayload(List<Long> categoryIds) {
}

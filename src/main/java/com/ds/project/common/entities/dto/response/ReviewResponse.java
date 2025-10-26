package com.ds.project.common.entities.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private String id;
    private String productId;
    private String userId;
    private String comment;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.ds.project.common.entities.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReviewRequest {
    @NotNull(message = "productId is required")
    private String productId;

    @NotBlank(message = "comment is required")
    private String comment;

    @NotNull(message = "rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;
}

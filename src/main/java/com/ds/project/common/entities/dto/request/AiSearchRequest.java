package com.ds.project.common.entities.dto.request;

import lombok.Data;

@Data
public class AiSearchRequest {
    private String query;
    private Double threshold;
    private Integer max_candidates;
    private Boolean rerank;
}


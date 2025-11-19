package com.ds.project.common.entities.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class AiSearchResponse {
    private String mode;
    private List<ResultItem> results;

    @Data
    public static class ResultItem {
        private String product_id;
        private String text;
        private Double score;
    }
}

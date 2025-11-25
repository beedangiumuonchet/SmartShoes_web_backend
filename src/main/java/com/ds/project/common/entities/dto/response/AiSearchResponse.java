package com.ds.project.common.entities.dto.response;

import lombok.Data;
import java.util.List;
import com.ds.project.app_context.models.*;

@Data
public class AiSearchResponse {
    private String mode;
    private List<ResultItem> results;
    private List<String> suggestions; // có thể null khi không fallback
    @Data
    public static class ResultItem {
        private String product_id;
        private String text;
        private Double score;
        private Integer stock;
        private Product.Status status ;
    }
}

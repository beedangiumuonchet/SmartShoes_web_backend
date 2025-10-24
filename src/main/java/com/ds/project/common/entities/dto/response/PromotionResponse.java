package com.ds.project.common.entities.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private String id;
    private String name;
    private String description;
    private Double percent;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}

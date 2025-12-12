package com.ds.project.common.entities.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private String id;
    private String userId;
    private List<CartDetailDto> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

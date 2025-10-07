package com.ds.project.common.entities.dto.request;

import com.ds.project.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {
    @NotNull
    private OrderStatus status;
}

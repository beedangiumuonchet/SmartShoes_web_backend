package com.ds.project.common.entities.dto.request;

import com.ds.project.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
    private OrderStatus status;
}

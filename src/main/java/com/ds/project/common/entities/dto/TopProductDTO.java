package com.ds.project.common.entities.dto;

import com.ds.project.app_context.models.Product;
import com.ds.project.common.entities.dto.response.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductDTO {
    private ProductResponse product;
    private int totalQuantity;
    private double totalRevenue;
}


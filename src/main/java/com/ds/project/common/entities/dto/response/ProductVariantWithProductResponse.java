package com.ds.project.common.entities.dto.response;

import lombok.*;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantWithProductResponse extends ProductVariantResponse {
    private ProductShortResponse product;
}

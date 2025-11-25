package com.ds.project.common.entities.dto.request;

import com.ds.project.common.entities.common.PaginationRequest;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProductFilterRequest extends PaginationRequest {
    private String q;                // Từ khóa tìm kiếm (name, slug,...)
    private String status;           // ACTIVE / INACTIVE

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAtFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAtTo;


    private Double minPrice;
    private Double maxPrice;

    private Boolean inStock; // true = còn hàng, false = hết hàng

    private List<String> brandIds;
    private List<String> categoryIds;

    private List<String> colorIds;
    private List<String> sizes;

    private List<String> attributeIds;
    private String attributeKey;
    private String attributeValue;

    private String sortBy;         // "createdAt" hoặc "price"
    private String sortDirection;  // "asc" hoặc "desc"


}

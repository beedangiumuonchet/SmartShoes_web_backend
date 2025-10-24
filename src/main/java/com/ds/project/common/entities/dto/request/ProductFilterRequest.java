package com.ds.project.common.entities.dto.request;

import com.ds.project.common.entities.common.PaginationRequest;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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

}

package com.ds.project.common.entities.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class OrderFilterRequest {

    private String q; // tìm theo mã đơn / user name / email

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate_from;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate_to;

    private String status; // ví dụ: PENDING, COMPLETED, CANCELLED
    private String userId; // lọc theo người dùng nếu cần

    private Integer page = 0;
    private Integer size = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}

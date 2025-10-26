package com.ds.project.common.entities.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentFilterRequest {
    private String q;        // tìm theo keyword (mã giao dịch, tên user, v.v.)
    private String status;   // trạng thái thanh toán
    private String method;   // phương thức thanh toán

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate_from;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate_to;

    private String userId;   // (nếu có lọc theo người dùng)
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}

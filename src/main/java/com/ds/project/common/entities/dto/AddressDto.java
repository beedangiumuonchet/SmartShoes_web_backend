package com.ds.project.common.entities.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String id;
    private String fullName;
    private String phone;
    private String street;
    private String ward;
    private String city;
    private Boolean isDefault;
}

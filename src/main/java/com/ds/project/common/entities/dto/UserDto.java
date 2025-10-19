package com.ds.project.common.entities.dto;

import com.ds.project.app_context.models.User;
import com.ds.project.common.enums.GenderStatus;
import com.ds.project.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * User Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private GenderStatus gender;
    private String phoneNumber;
    private UserStatus status;
    private Set<String> roles;
    private String createdAt;
    private String updatedAt;
}

package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Role;
import com.ds.project.app_context.models.User;
import com.ds.project.app_context.models.UserRoles;
import com.ds.project.app_context.repositories.RoleRepository;
import com.ds.project.app_context.repositories.UserRepository;
import com.ds.project.app_context.repositories.UserRolesRepository;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.base.Page;
import com.ds.project.common.entities.base.PagedData;
import com.ds.project.common.entities.dto.UserDto;
import com.ds.project.common.entities.dto.request.UserRequest;
import com.ds.project.common.entities.dto.request.ChangePasswordRequest;
import com.ds.project.common.interfaces.IUserService;
import com.ds.project.common.mapper.UserMapper;
import com.ds.project.common.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRolesRepository userRolesRepository;
    private final UserMapper userMapper;
    
    @Override
    public BaseResponse<UserDto> createUser(UserRequest userRequest) {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(userRequest.getEmail())) {
                return BaseResponse.<UserDto>builder()
                    .message(Optional.of("User with email " + userRequest.getEmail() + " already exists"))
                    .build();
            }
            if (userRepository.existsByUsername(userRequest.getUsername())) {
                return BaseResponse.<UserDto>builder()
                    .message(Optional.of("User with username " + userRequest.getUsername() + " already exists"))
                    .build();
            }
            
            User user = userMapper.mapToEntity(userRequest);
            user.setPassword(PasswordUtils.encodePassword(userRequest.getPassword()));
            
            // Assign roles if provided
            if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
                for (String roleName : userRequest.getRoles()) {
                    Optional<Role> roleOpt = roleRepository.findByName(roleName);
                    if (roleOpt.isPresent() && !roleOpt.get().getDeleted()) {
                        user.addRole(roleOpt.get());
                    } else {
                        log.warn("Role '{}' not found or deleted, skipping role assignment for user: {}", 
                            roleName, userRequest.getEmail());
                    }
                }
            }
            
            User savedUser = userRepository.save(user);
            
            // Force flush to ensure the user is persisted before mapping
            userRepository.flush();
            
            UserDto userDto = userMapper.mapToDto(savedUser);
            return BaseResponse.<UserDto>builder()
                .result(Optional.of(userDto))
                .build();
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return BaseResponse.<UserDto>builder()
                .message(Optional.of("Failed to create user: " + e.getMessage()))
                .build();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BaseResponse<UserDto>> getUserById(String id) {
        try {
            return userRepository.findById(id)
                .filter(user -> !user.getDeleted())
                .map(user -> {
                    UserDto userDto = userMapper.mapToDto(user);
                    return BaseResponse.<UserDto>builder()
                        .result(Optional.of(userDto))
                        .build();
                });
        } catch (Exception e) {
            log.error("Error getting user by id {}: {}", id, e.getMessage(), e);
            return Optional.of(BaseResponse.<UserDto>builder()
                .message(Optional.of("Failed to get user: " + e.getMessage()))
                .build());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BaseResponse<UserDto>> getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email)
                .filter(user -> !user.getDeleted())
                .map(user -> {
                    UserDto userDto = userMapper.mapToDto(user);
                    return BaseResponse.<UserDto>builder()
                        .result(Optional.of(userDto))
                        .build();
                });
        } catch (Exception e) {
            log.error("Error getting user by email {}: {}", email, e.getMessage(), e);
            return Optional.of(BaseResponse.<UserDto>builder()
                .message(Optional.of("Failed to get user: " + e.getMessage()))
                .build());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BaseResponse<UserDto>> getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username)
                .filter(user -> !user.getDeleted())
                .map(user -> {
                    UserDto userDto = userMapper.mapToDto(user);
                    return BaseResponse.<UserDto>builder()
                        .result(Optional.of(userDto))
                        .build();
                });
        } catch (Exception e) {
            log.error("Error getting user by username {}: {}", username, e.getMessage(), e);
            return Optional.of(BaseResponse.<UserDto>builder()
                .message(Optional.of("Failed to get user: " + e.getMessage()))
                .build());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PagedData<Page, UserDto>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getDeleted())
                .collect(Collectors.toList());
            
            List<UserDto> userDtos = users.stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
            
            Page page = Page.builder()
                .page(0)
                .size(userDtos.size())
                .totalElements((long) userDtos.size())
                .totalPages(1)
                .build();
            
            PagedData<Page, UserDto> pagedData = PagedData.<Page, UserDto>builder()
                .data(userDtos)
                .page(page)
                .build();
            
            return BaseResponse.<PagedData<Page, UserDto>>builder()
                .result(Optional.of(pagedData))
                .build();
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            return BaseResponse.<PagedData<Page, UserDto>>builder()
                .message(Optional.of("Failed to get users: " + e.getMessage()))
                .build();
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<UserDto> updateUser(String id, UserRequest userRequest) {
        try {
            User existingUser = userRepository.findById(id)
                .filter(user -> !user.getDeleted())
                .orElse(null);
            
            if (existingUser == null) {
                return BaseResponse.<UserDto>builder()
                    .message(Optional.of("User not found with id: " + id))
                    .build();
            }

            // Email
            if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
                if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
                        userRepository.existsByEmail(userRequest.getEmail())) {
                    return BaseResponse.<UserDto>builder()
                            .message(Optional.of("User with email " + userRequest.getEmail() + " already exists"))
                            .build();
                }
                existingUser.setEmail(userRequest.getEmail());
            }

// Username
            if (userRequest.getUsername() != null && !userRequest.getUsername().isBlank()) {
                if (!existingUser.getUsername().equals(userRequest.getUsername()) &&
                        userRepository.existsByUsername(userRequest.getUsername())) {
                    return BaseResponse.<UserDto>builder()
                            .message(Optional.of("User with username " + userRequest.getUsername() + " already exists"))
                            .build();
                }
                existingUser.setUsername(userRequest.getUsername());
            }

// First name & last name
            if (userRequest.getFirstName() != null) existingUser.setFirstName(userRequest.getFirstName());
            if (userRequest.getLastName() != null) existingUser.setLastName(userRequest.getLastName());

// Birthday, gender, phone
            if (userRequest.getBirthday() != null) existingUser.setBirthday(userRequest.getBirthday());
            if (userRequest.getGender() != null) existingUser.setGender(userRequest.getGender());
            if (userRequest.getPhoneNumber() != null) existingUser.setPhoneNumber(userRequest.getPhoneNumber());

// Status
            if (userRequest.getStatus() != null) existingUser.setStatus(userRequest.getStatus());

// Password — chỉ encode nếu có giá trị mới
            if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
                existingUser.setPassword(PasswordUtils.encodePassword(userRequest.getPassword()));
            }

// Roles — giữ nguyên logic hiện tại
            if (userRequest.getRoles() != null) {
                userRolesRepository.deleteByUserId(existingUser.getId());
                existingUser.getUserRoles().clear();

                for (String roleName : userRequest.getRoles()) {
                    Optional<Role> roleOpt = roleRepository.findByName(roleName);
                    if (roleOpt.isPresent() && !roleOpt.get().getDeleted()) {
                        existingUser.addRole(roleOpt.get());
                    } else {
                        log.warn("Role '{}' not found or deleted, skipping role assignment for user: {}",
                                roleName, existingUser.getEmail());
                    }
                }
            }


            User updatedUser = userRepository.save(existingUser);
            
            UserDto userDto = userMapper.mapToDto(updatedUser);
            return BaseResponse.<UserDto>builder()
                .result(Optional.of(userDto))
                .build();
        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage(), e);
            return BaseResponse.<UserDto>builder()
                .message(Optional.of("Failed to update user: " + e.getMessage()))
                .build();
        }
    }
    
    @Override
    @Transactional
    public void deleteUser(String id) {
        try {
            User user = userRepository.findById(id)
                .filter(u -> !u.getDeleted())
                .orElse(null);
            
            if (user == null) {
                log.warn("User not found with id: {}", id);
                return;
            }
            
            user.setDeleted(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("Successfully soft deleted user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", id, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<UserDto> assignRole(String userId, String roleId) {
        try {
            log.info("Assigning role {} to user {}", roleId, userId);

            // 1️⃣ Lấy user còn sống
            User user = userRepository.findById(userId)
                    .filter(u -> !u.getDeleted())
                    .orElse(null);

            if (user == null) {
                return BaseResponse.<UserDto>builder()
                        .message(Optional.of("User not found with id: " + userId))
                        .build();
            }

            // 2️⃣ Lấy role mới
            Role newRole = roleRepository.findById(roleId)
                    .filter(r -> !r.getDeleted())
                    .orElse(null);

            if (newRole == null) {
                return BaseResponse.<UserDto>builder()
                        .message(Optional.of("Role not found with id: " + roleId))
                        .build();
            }

            // 3️⃣ Xóa tất cả role cũ của user (nếu muốn chỉ có 1 role)
            if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                userRolesRepository.deleteAll(user.getUserRoles());
                user.getUserRoles().clear();
            }

            // 4️⃣ Tạo UserRoles mới cho role mới
            UserRoles userRole = UserRoles.builder()
                    .user(user)
                    .role(newRole)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRolesRepository.save(userRole);

            // ⚡ Quan trọng: thêm role mới vào user để mapper thấy
            if (user.getUserRoles() == null) {
                user.setUserRoles(new HashSet<>());
            }
            user.getUserRoles().add(userRole);

            // 5️⃣ Map về DTO
            UserDto userDto = userMapper.mapToDto(user);

            return BaseResponse.<UserDto>builder()
                    .result(Optional.of(userDto))
                    .message(Optional.of("Gán vai trò thành công"))
                    .build();

        } catch (Exception e) {
            log.error("Error assigning role {} to user {}: {}", roleId, userId, e.getMessage(), e);
            return BaseResponse.<UserDto>builder()
                    .message(Optional.of("Failed to assign role: " + e.getMessage()))
                    .build();
        }
    }



    @Override
    @Transactional
    public BaseResponse<UserDto> removeRole(String userId, String roleName) {
        try {
            // 1️⃣ Kiểm tra user tồn tại
            User user = userRepository.findById(userId)
                    .filter(u -> !u.getDeleted())
                    .orElse(null);

            if (user == null) {
                return BaseResponse.<UserDto>builder()
                        .message(Optional.of("User not found with id: " + userId))
                        .result(Optional.empty())
                        .build();
            }

            // 2️⃣ Tìm role theo TÊN thay vì ID
            Role role = roleRepository.findByName(roleName)
                    .filter(r -> !r.getDeleted())
                    .orElse(null);

            if (role == null) {
                return BaseResponse.<UserDto>builder()
                        .message(Optional.of("Role not found with name: " + roleName))
                        .build();
            }

            // 3️⃣ Kiểm tra user có role đó không
            UserRoles userRole = userRolesRepository.findByUserAndRole(user, role)
                    .orElse(null);

            if (userRole == null) {
                return BaseResponse.<UserDto>builder()
                        .message(Optional.of("User does not have role: " + role.getName()))
                        .build();
            }

            // 4️⃣ Xóa role khỏi user
            userRolesRepository.delete(userRole);
            user.removeRole(role);

            // 5️⃣ Trả lại DTO
            UserDto userDto = userMapper.mapToDto(user);
            return BaseResponse.<UserDto>builder()
                    .result(Optional.of(userDto))
                    .build();

        } catch (Exception e) {
            log.error("Error removing role {} from user {}: {}", roleName, userId, e.getMessage(), e);
            return BaseResponse.<UserDto>builder()
                    .message(Optional.of("Failed to remove role: " + e.getMessage()))
                    .build();
        }
    }


    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(String userId, String roleName) {
        try {
            User user = userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .orElse(null);
            if (user == null) {
                log.warn("User not found with id: {}", userId);
                return false;
            }
            
            return user.hasRole(roleName);
        } catch (Exception e) {
            log.error("Error checking role {} for user {}: {}", roleName, userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BaseResponse<UserDto>> authenticate(String username, String password) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username)
                .filter(user -> !user.getDeleted());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (PasswordUtils.matches(password, user.getPassword())) {
                    log.info("User authenticated successfully: {}", username);
                    UserDto userDto = userMapper.mapToDto(user);
                    return Optional.of(BaseResponse.<UserDto>builder()
                        .result(Optional.of(userDto))
                        .build());
                } else {
                    log.warn("Authentication failed for user: {} - invalid password", username);
                    return Optional.of(BaseResponse.<UserDto>builder()
                        .message(Optional.of("Invalid password"))
                        .build());
                }
            } else {
                log.warn("Authentication failed for user: {} - user not found", username);
                return Optional.of(BaseResponse.<UserDto>builder()
                    .message(Optional.of("User not found"))
                    .build());
            }
        } catch (Exception e) {
            log.error("Error authenticating user {}: {}", username, e.getMessage(), e);
            return Optional.of(BaseResponse.<UserDto>builder()
                .message(Optional.of("Authentication failed: " + e.getMessage()))
                .build());
        }
    }
    
    /**
     * Authenticate user by email or username
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BaseResponse<UserDto>> authenticateByEmailOrUsername(String emailOrUsername, String password) {
        try {
            Optional<User> userOpt = Optional.empty();
            
            // Try to find user by email first
            if (emailOrUsername.contains("@")) {
                userOpt = userRepository.findByEmail(emailOrUsername)
                    .filter(user -> !user.getDeleted());
            }
            
            // If not found by email, try username
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(emailOrUsername)
                    .filter(user -> !user.getDeleted());
            }
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (PasswordUtils.matches(password, user.getPassword())) {
                    log.info("User authenticated successfully: {}", emailOrUsername);
                    UserDto userDto = userMapper.mapToDto(user);
                    return Optional.of(BaseResponse.<UserDto>builder()
                        .result(Optional.of(userDto))
                        .build());
                } else {
                    log.warn("Authentication failed for user: {} - invalid password", emailOrUsername);
                    return Optional.of(BaseResponse.<UserDto>builder()
                        .message(Optional.of("Invalid password"))
                        .build());
                }
            } else {
                log.warn("Authentication failed for user: {} - user not found", emailOrUsername);
                return Optional.of(BaseResponse.<UserDto>builder()
                    .message(Optional.of("User not found"))
                    .build());
            }
        } catch (Exception e) {
            log.error("Error authenticating user {}: {}", emailOrUsername, e.getMessage(), e);
            return Optional.of(BaseResponse.<UserDto>builder()
                .message(Optional.of("Authentication failed: " + e.getMessage()))
                .build());
        }
    }

    @Override
    @Transactional
    public BaseResponse<Void> changePassword(String userId, ChangePasswordRequest request) {
        try {
            // 1️⃣ Lấy user
            User user = userRepository.findById(userId)
                    .filter(u -> !u.getDeleted())
                    .orElse(null);

            if (user == null) {
                return BaseResponse.<Void>builder()
                        .message(Optional.of("User not found with id: " + userId))
                        .build();
            }

            // 2️⃣ Kiểm tra mật khẩu cũ
            if (!PasswordUtils.matches(request.getOldPassword(), user.getPassword())) {
                return BaseResponse.<Void>builder()
                        .message(Optional.of("Old password is incorrect"))
                        .build();
            }

            // 3️⃣ Kiểm tra confirm password
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return BaseResponse.<Void>builder()
                        .message(Optional.of("New password and confirm password do not match"))
                        .build();
            }

            // 4️⃣ Không cho đổi sang mật khẩu cũ
            if (PasswordUtils.matches(request.getNewPassword(), user.getPassword())) {
                return BaseResponse.<Void>builder()
                        .message(Optional.of("New password must be different from old password"))
                        .build();
            }

            // 5️⃣ Encode & lưu
            user.setPassword(PasswordUtils.encodePassword(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            return BaseResponse.<Void>builder().build();

        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", userId, e.getMessage(), e);
            return BaseResponse.<Void>builder()
                    .message(Optional.of("Failed to change password: " + e.getMessage()))
                    .build();
        }
    }

}

package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Setting {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    /**
     * ⚠️ key là keyword của SQL → bắt buộc quote
     */
    @Column(name = "\"key\"", nullable = false, length = 255)
    private String key;

    /**
     * ⚠️ group là keyword → bắt buộc quote
     */
    @Column(name = "\"group\"", nullable = false, length = 255)
    private String group;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private SettingType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private SettingLevel level;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SettingType {
        STRING, INTEGER, BOOLEAN, JSON, ARRAY
    }

    public enum SettingLevel {
        SYSTEM, USER, ORGANIZATION
    }
}

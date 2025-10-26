package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Lưu relation tới Product
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Lưu relation tới User (nếu có entity User). Nếu bạn chỉ muốn lưu userId string, đổi sang @Column String userId.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "text")
    private String comment;

    private Integer rating; // 1..5

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

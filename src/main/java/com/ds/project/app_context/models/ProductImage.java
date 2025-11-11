package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "varchar", nullable = false, updatable = false)
    private String id;

    private String url;

    @Column(name = "is_main")
    private Boolean isMain;

    // Nếu ảnh liên kết với ProductVariant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    // 🖼 Vector embedding của ảnh (PostgreSQL double precision array)
    @Column(name = "embedding", columnDefinition = "double precision[]")
    private Double[] embedding; // Lưu vector sau khi xử lý ảnh
}

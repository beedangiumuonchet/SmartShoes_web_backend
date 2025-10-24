package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(unique = true, nullable = false)
    private String slug;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductVariant> variants;

//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
//    private Set<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductAttribute> productAttributes;

    public enum Status {
        ACTIVE,        // Đang kinh doanh
        INACTIVE,      // Ngừng kinh doanh
        OUT_OF_STOCK  // Hết hàng
    }
}

package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "color_id", "size"})
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "varchar", nullable = false, updatable = false)
    private String id;

    @ManyToOne  
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @Column(nullable = false)
    private String size;

    private Double price;
    private Double priceSale;

    private Integer stock;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.priceSale == null) {
            this.priceSale = this.price; // Giá sale mặc định bằng giá gốc khi tạo
        }
    }
}

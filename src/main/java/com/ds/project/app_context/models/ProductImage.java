package com.ds.project.app_context.models;

import com.ds.project.app_context.converters.FloatArrayConverter;
import com.ds.project.app_context.converters.PgVectorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

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
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    private String url;

    @Column(name = "is_main")
    private Boolean isMain;

    // Nếu ảnh liên kết với ProductVariant
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    // 🖼 Vector embedding của ảnh (PostgreSQL double precision array)
    @Type(value = PgVectorType.class)
    @Column(name = "embedding", columnDefinition = "vector(256)")
//    @Convert(converter = FloatArrayConverter.class)
    private float[] embedding;
}

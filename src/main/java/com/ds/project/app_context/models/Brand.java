package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.Set;

@Entity
@Table(name = "brands")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "varchar", updatable = false, nullable = false)
    private String id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private Set<Product> products;
}

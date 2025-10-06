package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "attributes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "key_name")
    private String key;

    private String value;

    private String description;
}

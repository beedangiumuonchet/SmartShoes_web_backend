package com.ds.project.app_context.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "attributes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "varchar", updatable = false, nullable = false)
    private String id;

    @Column(name = "key_name")
    private String key;

    private String value;

    private String description;
}

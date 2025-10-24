package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Attribute;
import com.ds.project.common.entities.dto.request.AttributeRequest;
import com.ds.project.common.entities.dto.response.AttributeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Attribute entity and DTOs
 */
@Component
@RequiredArgsConstructor
public class AttributeMapper {

    /**
     * Map request DTO -> entity
     */
    public Attribute mapToEntity(AttributeRequest request) {
        if (request == null) return null;
        return Attribute.builder()
                .key(request.getKey())
                .value(request.getValue())
                .description(request.getDescription())
                .build();
    }

    /**
     * Map entity -> response DTO
     */
    public AttributeResponse mapToDto(Attribute entity) {
        if (entity == null) return null;
        return AttributeResponse.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .value(entity.getValue())
                .description(entity.getDescription())
                .build();
    }

    /**
     * Map list of entities -> list of response DTOs
     */
    public List<AttributeResponse> mapToDtoList(List<Attribute> attributes) {
        if (attributes == null) return List.of();
        return attributes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}

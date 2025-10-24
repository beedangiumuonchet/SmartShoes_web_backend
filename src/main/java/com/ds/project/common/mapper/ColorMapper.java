package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Color;
import com.ds.project.common.entities.dto.request.ColorRequest;
import com.ds.project.common.entities.dto.response.ColorResponse;
import org.springframework.stereotype.Component;

@Component
public class ColorMapper {

    public Color toEntity(ColorRequest request) {
        return Color.builder()
                .name(request.getName())
                .build();
    }

    public ColorResponse toResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId())
                .name(color.getName())
                .build();
    }
}

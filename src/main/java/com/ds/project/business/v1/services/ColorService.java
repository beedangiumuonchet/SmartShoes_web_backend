package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Color;
import com.ds.project.app_context.repositories.ColorRepository;
import com.ds.project.common.entities.dto.request.ColorRequest;
import com.ds.project.common.entities.dto.response.ColorResponse;
import com.ds.project.common.interfaces.IColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorService implements IColorService {

    private final ColorRepository colorRepository;

    @Override
    public ColorResponse createColor(ColorRequest request) {
        if (colorRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Color with name '" + request.getName() + "' already exists");
        }

        Color color = new Color();
        color.setName(request.getName());

        Color saved = colorRepository.save(color);
        return mapToResponse(saved);
    }

    @Override
    public List<ColorResponse> getAllColors() {
        return colorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ColorResponse getColorById(String id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Color not found with id: " + id));
        return mapToResponse(color);
    }

    @Override
    public ColorResponse updateColor(String id, ColorRequest request) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Color not found with id: " + id));

        color.setName(request.getName());
        Color updated = colorRepository.save(color);
        return mapToResponse(updated);
    }

    @Override
    public void deleteColor(String id) {
        if (!colorRepository.existsById(id)) {
            throw new EntityNotFoundException("Color not found with id: " + id);
        }
        colorRepository.deleteById(id);
    }

    private ColorResponse mapToResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId())
                .name(color.getName())
                .build();
    }
}

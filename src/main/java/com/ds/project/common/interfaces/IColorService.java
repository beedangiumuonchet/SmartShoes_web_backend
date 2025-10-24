package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.ColorRequest;
import com.ds.project.common.entities.dto.response.ColorResponse;

import java.util.List;

public interface IColorService {
    ColorResponse createColor(ColorRequest request);
    List<ColorResponse> getAllColors();
    ColorResponse getColorById(String id);
    ColorResponse updateColor(String id, ColorRequest request);
    void deleteColor(String id);
}

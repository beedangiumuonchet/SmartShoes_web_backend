package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.dto.request.ColorRequest;
import com.ds.project.common.entities.dto.response.ColorResponse;
import com.ds.project.common.interfaces.IColorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final IColorService colorService;

    @PostMapping
    public ResponseEntity<?> createColor(@RequestBody ColorRequest request) {
        try {
            ColorResponse response = colorService.createColor(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to create color: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllColors() {
        try {
            List<ColorResponse> colors = colorService.getAllColors();
            return ResponseEntity.ok(colors);
        } catch (Exception e) {
            log.error("❌ Failed to fetch colors: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getColorById(@PathVariable String id) {
        try {
            ColorResponse response = colorService.getColorById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to fetch color {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateColor(@PathVariable String id, @RequestBody ColorRequest request) {
        try {
            ColorResponse response = colorService.updateColor(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to update color {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColor(@PathVariable String id) {
        try {
            colorService.deleteColor(id);
            return ResponseEntity.ok("✅ Color deleted successfully");
        } catch (Exception e) {
            log.error("❌ Failed to delete color {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

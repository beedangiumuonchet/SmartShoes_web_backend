package com.ds.project.application.controllers.v1;

import com.ds.project.business.v1.services.AttributeService;
import com.ds.project.common.entities.dto.request.AttributeRequest;
import com.ds.project.common.entities.dto.response.AttributeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Attributes
 */
@RestController
@RequestMapping("/api/attributes")
@RequiredArgsConstructor
@Slf4j
public class AttributeController {

    private final AttributeService attributeService;

    /**
     * Create a new attribute
     */
    @PostMapping
    public ResponseEntity<?> createAttribute(@RequestBody AttributeRequest request) {
        try {
            AttributeResponse response = attributeService.createAttribute(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to create attribute: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create attribute: " + e.getMessage());
        }
    }

    /**
     * Get all attributes
     */
    @GetMapping
    public ResponseEntity<?> getAllAttributes() {
        try {
            List<AttributeResponse> attributes = attributeService.getAllAttributes();
            return ResponseEntity.ok(attributes);
        } catch (Exception e) {
            log.error("❌ Failed to fetch attributes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch attributes: " + e.getMessage());
        }
    }

    /**
     * Get attribute by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAttributeById(@PathVariable String id) {
        try {
            AttributeResponse response = attributeService.getAttributeById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to get attribute {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to get attribute: " + e.getMessage());
        }
    }

    /**
     * Update attribute
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttribute(@PathVariable String id, @RequestBody AttributeRequest request) {
        try {
            AttributeResponse response = attributeService.updateAttribute(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to update attribute {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update attribute: " + e.getMessage());
        }
    }

    /**
     * Delete attribute
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttribute(@PathVariable String id) {
        try {
            attributeService.deleteAttribute(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ Failed to delete attribute {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to delete attribute: " + e.getMessage());
        }
    }
}

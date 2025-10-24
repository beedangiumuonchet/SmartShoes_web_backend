package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Attribute;
import com.ds.project.app_context.repositories.AttributeRepository;
import com.ds.project.common.entities.dto.request.AttributeRequest;
import com.ds.project.common.entities.dto.response.AttributeResponse;
import com.ds.project.common.interfaces.IAttributeService;
import com.ds.project.common.mapper.AttributeMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttributeService implements IAttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    @Override
    public AttributeResponse createAttribute(AttributeRequest request) {
        try {
            if (attributeRepository.existsByKeyIgnoreCaseAndValueIgnoreCase(request.getKey(), request.getValue())) {
                throw new IllegalArgumentException(
                        String.format("Attribute with key='%s' and value='%s' already exists", request.getKey(), request.getValue())
                );
            }

            Attribute attribute = attributeMapper.mapToEntity(request);
            Attribute saved = attributeRepository.save(attribute);
            log.info("✅ Created new attribute: key={}, value={}", request.getKey(), request.getValue());
            return attributeMapper.mapToDto(saved);
        } catch (Exception e) {
            log.error("❌ Failed to create attribute: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create attribute: " + e.getMessage());
        }
    }

    @Override
    public AttributeResponse updateAttribute(String id, AttributeRequest request) {
        try {
            Attribute existing = attributeRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

            // ✅ Kiểm tra nếu đổi key/value trùng với bản ghi khác
            boolean isDuplicate = attributeRepository.existsByKeyIgnoreCaseAndValueIgnoreCase(request.getKey(), request.getValue());
            if (isDuplicate && (!existing.getKey().equalsIgnoreCase(request.getKey())
                    || !existing.getValue().equalsIgnoreCase(request.getValue()))) {
                throw new IllegalArgumentException(
                        String.format("Another attribute already has key='%s' and value='%s'", request.getKey(), request.getValue())
                );
            }

            existing.setKey(request.getKey());
            existing.setValue(request.getValue());
            existing.setDescription(request.getDescription());

            Attribute updated = attributeRepository.save(existing);
            log.info("📝 Updated attribute: id={}, key={}", id, request.getKey());
            return attributeMapper.mapToDto(updated);
        } catch (Exception e) {
            log.error("❌ Failed to update attribute {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update attribute: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeResponse getAttributeById(String id) {
        try {
            Attribute attribute = attributeRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
            return attributeMapper.mapToDto(attribute);
        } catch (Exception e) {
            log.error("❌ Failed to fetch attribute {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get attribute: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAllAttributes() {
        try {
            List<Attribute> attributes = attributeRepository.findAll();
            log.info("📦 Fetched {} attributes", attributes.size());
            return attributeMapper.mapToDtoList(attributes);
        } catch (Exception e) {
            log.error("❌ Failed to fetch attributes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch attributes: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String id) {
        try {
            if (!attributeRepository.existsById(id)) {
                throw new EntityNotFoundException("Attribute not found");
            }
            attributeRepository.deleteById(id);
            log.info("🗑️ Deleted attribute successfully: id={}", id);
        } catch (Exception e) {
            log.error("❌ Failed to delete attribute {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete attribute: " + e.getMessage());
        }
    }
}

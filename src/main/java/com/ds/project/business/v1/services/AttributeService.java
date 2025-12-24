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

        String key = request.getKey().trim();
        String value = request.getValue().trim();

        if (attributeRepository.existsByKeyIgnoreCaseAndValueIgnoreCase(key, value)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Attribute with key='%s' and value='%s' already exists",
                            key, value
                    )
            );
        }

        Attribute attribute = attributeMapper.mapToEntity(request);
        attribute.setKey(key);
        attribute.setValue(value);

        Attribute saved = attributeRepository.save(attribute);

        log.info("✅ Created new attribute: key={}, value={}", key, value);
        return attributeMapper.mapToDto(saved);
    }


    @Override
    public AttributeResponse updateAttribute(String id, AttributeRequest request) {

        Attribute existing = attributeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

        String newKey = request.getKey().trim();
        String newValue = request.getValue().trim();

        boolean isKeyOrValueChanged =
                !newKey.equalsIgnoreCase(existing.getKey()) ||
                        !newValue.equalsIgnoreCase(existing.getValue());

        // ❗ CHECK TRÙNG (IGNORE CASE + KHÁC ID)
        if (isKeyOrValueChanged &&
                attributeRepository.existsByKeyIgnoreCaseAndValueIgnoreCaseAndIdNot(
                        newKey, newValue, id
                )) {

            throw new IllegalArgumentException(
                    String.format(
                            "Another attribute already has key='%s' and value='%s'",
                            newKey, newValue
                    )
            );
        }

        existing.setKey(newKey);
        existing.setValue(newValue);
        existing.setDescription(request.getDescription());

        Attribute updated = attributeRepository.save(existing);

        log.info("📝 Updated attribute: id={}, key={}, value={}", id, newKey, newValue);
        return attributeMapper.mapToDto(updated);
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

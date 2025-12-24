package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Brand;
import com.ds.project.app_context.repositories.BrandRepository;
import com.ds.project.common.entities.dto.request.BrandRequest;
import com.ds.project.common.entities.dto.response.BrandResponse;
import com.ds.project.common.interfaces.IBrandService;
import com.ds.project.common.mapper.BrandMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService implements IBrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper mapper;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {

        String name = request.getName().trim();

        if (brandRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new IllegalArgumentException("Brand name already exists");
        }

        Brand brand = mapper.toEntity(request);
        brand.setName(name);

        // 👉 sinh slug từ name
        String slug = toSlug(name);

        // 👉 đảm bảo slug unique
        if (brandRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        brand.setSlug(slug);

        Brand saved = brandRepository.save(brand);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(String id, BrandRequest request) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Brand not found with id=" + id)
                );

        String newName = request.getName().trim();

        boolean isNameChanged =
                newName != null &&
                        !newName.equalsIgnoreCase(brand.getName());

        // ❗ CHECK TRÙNG NAME (IGNORE CASE)
        if (isNameChanged &&
                brandRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {

            throw new IllegalArgumentException("Brand name already exists");
        }

        brand.setName(newName);

        // 👉 nếu đổi name → sinh lại slug
        if (isNameChanged) {
            String newSlug = toSlug(newName);

            if (brandRepository.existsBySlugAndIdNot(newSlug, id)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }

            log.info("📦 Updated brand slug = {}", newSlug);
            brand.setSlug(newSlug);
        }

        Brand updated = brandRepository.save(brand);
        return mapper.toResponse(updated);
    }


    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(String id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id=" + id));
        return mapper.toResponse(brand);
    }
    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new EntityNotFoundException("Brand not found with slug=" + slug)
                );

        return mapper.toResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteBrand(String id) {
        if (!brandRepository.existsById(id)) {
            throw new EntityNotFoundException("Brand not found id=" + id);
        }
        brandRepository.deleteById(id);
    }

    public String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}

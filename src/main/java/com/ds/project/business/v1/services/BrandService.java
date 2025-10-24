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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (brandRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Brand name already exists");
        }

        Brand brand = mapper.toEntity(request);
        Brand saved = brandRepository.save(brand);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(String id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id=" + id));

        mapper.updateEntity(brand, request);
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
}

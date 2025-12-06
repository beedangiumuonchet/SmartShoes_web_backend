package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.ProductImage;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.app_context.repositories.ProductImageRepository;
import com.ds.project.app_context.repositories.ProductVariantRepository;
import com.ds.project.common.entities.dto.request.ProductImageRequest;
import com.ds.project.common.entities.dto.response.ProductImageResponse;
import com.ds.project.common.interfaces.IProductImageService;
import com.ds.project.common.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService implements IProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageMapper productImageMapper;

    @Override
    public ProductImageResponse create(ProductImageRequest request, String productVariantId) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm với id: " + productVariantId));

        ProductImage entity = productImageMapper.mapToEntity(request);
        entity.setProductVariant(variant);

        ProductImage saved = productImageRepository.save(entity);
        return productImageMapper.mapToDto(saved);
    }

    @Override
    public ProductImageResponse update(String id, ProductImageRequest request) {
        ProductImage entity = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh sản phẩm với id: " + id));

//        entity.setUrl(request.getUrl());
        entity.setIsMain(request.getIsMain());

        ProductImage updated = productImageRepository.save(entity);
        return productImageMapper.mapToDto(updated);
    }

    @Override
    public void delete(String id) {
        if (!productImageRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy ảnh sản phẩm với id: " + id);
        }
        productImageRepository.deleteById(id);
    }

    @Override
    public List<ProductImageResponse> getAllByVariantId(String productVariantId) {
        return productImageRepository.findByProductVariantId(productVariantId)
                .stream()
                .map(productImageMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductImageResponse getById(String id) {
        Optional<ProductImage> img = productImageRepository.findById(id);
        System.out.println("Found? " + img.isPresent());

        ProductImage entity = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh sản phẩm với id: " + id));
        return productImageMapper.mapToDto(entity);
    }
}

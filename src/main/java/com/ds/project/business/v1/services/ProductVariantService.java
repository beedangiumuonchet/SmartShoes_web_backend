package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Color;
import com.ds.project.app_context.models.Product;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.app_context.repositories.ColorRepository;
import com.ds.project.app_context.repositories.ProductRepository;
import com.ds.project.app_context.repositories.ProductVariantRepository;
import com.ds.project.common.entities.dto.request.ProductVariantRequest;
import com.ds.project.common.entities.dto.response.ProductVariantResponse;
import com.ds.project.common.entities.dto.response.ProductVariantWithProductResponse;
import com.ds.project.common.interfaces.IProductVariantService;
import com.ds.project.common.mapper.ProductVariantMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService implements IProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final ProductVariantMapper productVariantMapper;

    @Override
    public ProductVariantResponse createVariant(String productId, ProductVariantRequest request) {
        // Tìm product và color hợp lệ
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new EntityNotFoundException("Color not found"));

        // Kiểm tra trùng variant (color + size)
        boolean exists = productVariantRepository.existsByProductIdAndColorIdAndSize(productId, request.getColorId(), request.getSize());
        if (exists) {
            throw new IllegalArgumentException("Variant with same color and size already exists for this product");
        }

        // Map sang entity
        ProductVariant variant = productVariantMapper.mapToEntity(request, product, color);

        // Lưu và trả về DTO
        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.mapToDto(saved);
    }

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(String productId) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        return productVariantMapper.mapToDtoList(variants);
    }

    @Override
    public ProductVariantResponse updateVariant(String id, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));

        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new EntityNotFoundException("Color not found"));

        // Cập nhật các trường
        variant.setColor(color);
        variant.setSize(request.getSize());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());

        ProductVariant updated = productVariantRepository.save(variant);
        return productVariantMapper.mapToDto(updated);
    }

    @Override
    public ProductVariantResponse getVariantById(String id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product variant not found" + id));
        return productVariantMapper.mapToDto(variant);
    }

    @Override
    public ProductVariantWithProductResponse getVariantWithProductById(String id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product variant not found " + id));
        return productVariantMapper.mapToDtoWithProduct(variant);
    }

    @Override
    public void deleteVariant(String id) {
        if (!productVariantRepository.existsById(id)) {
            throw new EntityNotFoundException("Product variant not found");
        }
        productVariantRepository.deleteById(id);
    }
}

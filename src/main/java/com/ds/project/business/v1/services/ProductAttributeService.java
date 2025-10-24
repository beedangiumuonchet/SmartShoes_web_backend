package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.*;
import com.ds.project.app_context.repositories.*;
import com.ds.project.common.entities.dto.request.ProductAttributeRequest;
import com.ds.project.common.entities.dto.response.ProductAttributeResponse;
import com.ds.project.common.interfaces.IProductAttributeService;
import com.ds.project.common.mapper.ProductAttributeMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAttributeService implements IProductAttributeService {

    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeMapper mapper;

    @Override
    @Transactional
    public ProductAttributeResponse addProductAttribute(String productId, ProductAttributeRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id=" + productId));

        Attribute attribute = attributeRepository.findById(request.getAttributeId())
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id=" + request.getAttributeId()));

        // Kiểm tra trùng
        if (productAttributeRepository.existsByProduct_IdAndAttribute_Id(productId, request.getAttributeId())) {
            throw new IllegalArgumentException("This attribute is already assigned to the product");
        }

        ProductAttribute pa = ProductAttribute.builder()
                .product(product)
                .attribute(attribute)
                .build();

        ProductAttribute saved = productAttributeRepository.save(pa);

        log.info("✅ Added attribute [{}:{}] to product [{}]", attribute.getKey(), attribute.getValue(), product.getName());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeResponse> getProductAttributes(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product not found with id=" + productId);
        }
        List<ProductAttribute> list = productAttributeRepository.findByProduct_Id(productId);
        return list.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteProductAttribute(String id) {
        if (!productAttributeRepository.existsById(id)) {
            throw new EntityNotFoundException("ProductAttribute not found id=" + id);
        }
        productAttributeRepository.deleteById(id);
        log.info("🗑️ Deleted product attribute id={}", id);
    }
}

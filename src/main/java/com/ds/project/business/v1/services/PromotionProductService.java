package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Promotion;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.app_context.models.PromotionProduct;
import com.ds.project.app_context.repositories.PromotionProductRepository;
import com.ds.project.app_context.repositories.PromotionRepository;
import com.ds.project.app_context.repositories.ProductVariantRepository;
import com.ds.project.common.entities.dto.request.PromotionProductRequest;
import com.ds.project.common.entities.dto.response.PromotionProductResponse;
import com.ds.project.common.interfaces.IPromotionProductService;
import com.ds.project.common.mapper.PromotionProductMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionProductService implements IPromotionProductService {

    private final PromotionProductRepository promotionProductRepository;
    private final PromotionRepository promotionRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromotionProductMapper mapper;

    @Override
    public PromotionProductResponse create(PromotionProductRequest request) {
        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khuyến mãi với id=" + request.getPromotionId()));

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biến thể sản phẩm với id=" + request.getProductVariantId()));

        PromotionProduct entity = mapper.mapToEntity(request, promotion, variant);
        PromotionProduct saved = promotionProductRepository.save(entity);
        return mapper.mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionProductResponse getById(String id) {
        PromotionProduct entity = promotionProductRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy liên kết PromotionProduct id=" + id));
        return mapper.mapToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionProductResponse> getAll() {
        return promotionProductRepository.findAll().stream().map(mapper::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionProductResponse> getByPromotionId(String promotionId) {
        return promotionProductRepository.findAll().stream()
                .filter(p -> p.getPromotion() != null && promotionId.equals(p.getPromotion().getId()))
                .map(mapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionProductResponse> getByProductVariantId(String productVariantId) {
        return promotionProductRepository.findAll().stream()
                .filter(p -> p.getProductVariant() != null && productVariantId.equals(p.getProductVariant().getId()))
                .map(mapper::mapToDto)
                .toList();
    }

    @Override
    public void delete(String id) {
        if (!promotionProductRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy PromotionProduct id=" + id);
        }
        promotionProductRepository.deleteById(id);
    }
}

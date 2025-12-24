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
import com.ds.project.common.interfaces.IPromotionService;
import com.ds.project.common.mapper.PromotionProductMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionProductService implements IPromotionProductService {

    private final PromotionProductRepository promotionProductRepository;
    private final PromotionRepository promotionRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromotionProductMapper mapper;
    private final PromotionService promotionService;

    @Override
    public PromotionProductResponse create(PromotionProductRequest request) {
        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khuyến mãi với id=" + request.getPromotionId()));

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biến thể sản phẩm với id=" + request.getProductVariantId()));

        PromotionProduct entity = mapper.mapToEntity(request, promotion, variant);
        PromotionProduct saved = promotionProductRepository.save(entity);

        // Cập nhật priceSale ngay khi tạo
//        updatePriceSaleForVariant(variant);

        promotionService.recalculatePriceSaleForVariant(variant);

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
        PromotionProduct pp = promotionProductRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy PromotionProduct id=" + id));

        ProductVariant variant = pp.getProductVariant();

        promotionProductRepository.delete(pp);

        // ⭐ BẮT BUỘC: tính lại giá sau khi xoá liên kết
        promotionService.recalculatePriceSaleForVariant(variant);
    }


    /**
     * Cập nhật priceSale cho 1 ProductVariant dựa trên Promotion active
     */
//    private void updatePriceSaleForVariant(ProductVariant variant) {
//        if (variant == null) return;
//
//        LocalDate today = LocalDate.now();
//
//        // Lấy tất cả PromotionProduct của variant này
//        List<PromotionProduct> activePPs = promotionProductRepository.findByProductVariantId(variant.getId());
//
//        // Chỉ lấy promotions đang active và trong ngày
//        double maxDiscount = 0;
//        for (PromotionProduct pp : activePPs) {
//            Promotion promo = pp.getPromotion();
//            if (promo != null
//                    && promo.getStatus() == Promotion.PromotionStatus.ACTIVE
//                    && !today.isBefore(promo.getStartDate())
//                    && !today.isAfter(promo.getEndDate())) {
//                double discount = variant.getPrice() * promo.getPercent() / 100;
//                if (discount > maxDiscount) maxDiscount = discount;
//            }
//        }
//
//        if (maxDiscount > 0) {
//            variant.setPriceSale(variant.getPrice() - maxDiscount);
//        } else {
//            variant.setPriceSale(variant.getPrice()); // reset về giá gốc
//        }
//
//        productVariantRepository.save(variant);
//    }

    /**
     * Cập nhật priceSale tự động mỗi ngày lúc 0h
     */
    @Scheduled(cron = "0 4 15 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void recalculateAllPricesDaily() {
        List<ProductVariant> variants = productVariantRepository.findAll();
        for (ProductVariant v : variants) {
            promotionService.recalculatePriceSaleForVariant(v);
        }
    }

//    public void updatePriceSaleDaily() {
//        LocalDate today = LocalDate.now();
//        System.out.println(">>> [SCHEDULED JOB] updatePriceSaleDaily started at " + LocalDateTime.now());
//        System.out.println(">>> [SCHEDULED JOB] Today = " + today);
//
//        List<PromotionProduct> activePPs = promotionProductRepository.findAll();
//
//        int updated = 0;
//        int reset = 0;
//
//        for (PromotionProduct pp : activePPs) {
//            if (pp.getPromotion() != null && pp.getProductVariant() != null) {
//                var promo = pp.getPromotion();
//                var product = pp.getProductVariant();
//
//                if (promo.getStatus() == Promotion.PromotionStatus.ACTIVE
//                        && !today.isBefore(promo.getStartDate())
//                        && !today.isAfter(promo.getEndDate())) {
//
//                    double discount = product.getPrice() * promo.getPercent() / 100;
//                    product.setPriceSale(product.getPrice() - discount);
//                    productVariantRepository.save(product);
//                    updated++;
//                }
//            }
//        }
//
//        for (PromotionProduct pp : activePPs) {
//            if (pp.getPromotion() != null && pp.getProductVariant() != null) {
//                var promo = pp.getPromotion();
//                var product = pp.getProductVariant();
//
//                if (promo.getStatus() == Promotion.PromotionStatus.EXPIRED
//                        || today.isAfter(promo.getEndDate())) {
//                    product.setPriceSale(product.getPrice());
//                    productVariantRepository.save(product);
//                    reset++;
//                }
//            }
//        }
//
//        System.out.println(">>> [SCHEDULED JOB] updatePriceSaleDaily finished.");
//        System.out.println(">>> [SCHEDULED JOB] Updated: " + updated + ", Reset: " + reset);
//    }

}

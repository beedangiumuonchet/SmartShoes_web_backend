package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Promotion;
import com.ds.project.app_context.repositories.PromotionRepository;
import com.ds.project.common.entities.dto.request.PromotionRequest;
import com.ds.project.common.entities.dto.response.PromotionResponse;
import com.ds.project.common.interfaces.IPromotionService;
import com.ds.project.common.mapper.PromotionMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService implements IPromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper mapper;

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        // Validate ngày tháng
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Promotion promotion = mapper.mapToEntity(request);

        // Auto set trạng thái theo ngày
        LocalDate today = LocalDate.now();
        if (promotion.getStartDate().isAfter(today)) {
            promotion.setStatus(Promotion.PromotionStatus.UPCOMING);
        } else if (promotion.getEndDate().isBefore(today)) {
            promotion.setStatus(Promotion.PromotionStatus.EXPIRED);
        } else {
            promotion.setStatus(Promotion.PromotionStatus.ACTIVE);
        }

        Promotion saved = promotionRepository.save(promotion);
        return mapper.mapToDto(saved);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(String id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with id=" + id));

        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setPercent(request.getPercent());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            promotion.setStatus(Promotion.PromotionStatus.valueOf(request.getStatus().toUpperCase()));
        }

        Promotion updated = promotionRepository.save(promotion);
        return mapper.mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(String id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with id=" + id));
        return mapper.mapToDto(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll()
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deletePromotion(String id) {
        if (!promotionRepository.existsById(id)) {
            throw new EntityNotFoundException("Promotion not found id=" + id);
        }
        promotionRepository.deleteById(id);
    }

    /**
     * Chạy mỗi ngày lúc 0h
     */
    @Scheduled(cron = "0 2 15 * * ?") // 0 giây, 0 phút, 0 giờ, mỗi ngày
    @Transactional
    public void updatePromotionStatuses() {
        log.info("PromotionStatusJob started at {}", LocalDate.now());

        List<Promotion> promotions = promotionRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Promotion p : promotions) {
            Promotion.PromotionStatus oldStatus = p.getStatus();
            Promotion.PromotionStatus newStatus = determineStatus(p, today);

            if (oldStatus != newStatus) {
                p.setStatus(newStatus);
                promotionRepository.save(p);
                log.info("Promotion {} status changed from {} to {}", p.getId(), oldStatus, newStatus);
            }
        }

        log.info("PromotionStatusJob finished");
    }

    /**
     * Xác định status mới dựa trên ngày hôm nay
     */
    private Promotion.PromotionStatus determineStatus(Promotion p, LocalDate today) {
        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) {
            return Promotion.PromotionStatus.UPCOMING;
        } else if (p.getEndDate() != null && today.isAfter(p.getEndDate())) {
            return Promotion.PromotionStatus.EXPIRED;
        } else if (p.getStatus() != Promotion.PromotionStatus.INACTIVE) {
            // Nếu không bị INACTIVE tạm dừng thì ACTIVE
            return Promotion.PromotionStatus.ACTIVE;
        } else {
            return p.getStatus(); // giữ nguyên INACTIVE
        }
    }
}

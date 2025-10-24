package com.ds.project.application.startup.data;

import com.ds.project.app_context.models.*;
import com.ds.project.app_context.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStartup {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final AttributeRepository attributeRepository;

    @Value("${app.startup.create-dev-datas:false}")
    private boolean createDevData;

    public void initializeProductBaseData() {
        log.info("Initializing product startup data...");

        try {
            createBrands();
            createCategories();
            createColors();
            createAttributes();

            log.info("✅ Product base data initialization completed");
        } catch (Exception e) {
            log.error("❌ Failed to initialize product startup data: {}", e.getMessage(), e);
            throw e;
        }
    }

    // -----------------------------
    //  BRAND
    // -----------------------------
    private void createBrands() {
        if (!createDevData) return;

        log.info("Creating default brands...");
        List<String> defaultBrands = List.of("Nike", "Adidas", "Puma", "Converse", "Vans");

        for (String brandName : defaultBrands) {
            brandRepository.findByNameIgnoreCase(brandName)
                    .orElseGet(() -> {
                        Brand brand = Brand.builder()
                                .name(brandName)
                                .build();
                        brandRepository.save(brand);
                        log.info("🟢 Created brand: {} (id={})", brandName, brand.getId());
                        return brand;
                    });
        }
    }

    // -----------------------------
    //  CATEGORY
    // -----------------------------
    private void createCategories() {
        if (!createDevData) return;

        log.info("Creating default categories...");
        List<String> defaultCategories = List.of("Giày thể thao", "Giày da", "Dép", "Phụ kiện", "Áo quần");

        for (String categoryName : defaultCategories) {
            categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseGet(() -> {
                        Category category = Category.builder()
                                .name(categoryName)
                                .build();
                        categoryRepository.save(category);
                        log.info("🟢 Created category: {} (id={})", categoryName, category.getId());
                        return category;
                    });
        }
    }

    // -----------------------------
    //  COLOR
    // -----------------------------
    private void createColors() {
        if (!createDevData) return;

        log.info("Creating default colors...");
        List<String> defaultColors = List.of("Đỏ", "Xanh", "Trắng", "Đen", "Vàng", "Tím");

        for (String colorName : defaultColors) {
            colorRepository.findByNameIgnoreCase(colorName)
                    .orElseGet(() -> {
                        Color color = Color.builder()
                                .name(colorName)
                                .build();
                        colorRepository.save(color);
                        log.info("🟢 Created color: {} (id={})", colorName, color.getId());
                        return color;
                    });
        }
    }

    // -----------------------------
    //  ATTRIBUTE
    // -----------------------------
    private void createAttributes() {
        if (!createDevData) return;

        log.info("Creating default attributes...");
        List<Attribute> defaultAttrs = List.of(
                Attribute.builder().key("Chất liệu").value("Da cao cấp").description("Chất liệu giày da thật").build(),
                Attribute.builder().key("Xuất xứ").value("Việt Nam").description("Sản xuất tại Việt Nam").build(),
                Attribute.builder().key("Phong cách").value("Thể thao").description("Thiết kế năng động, thoải mái").build()
        );

        for (Attribute attr : defaultAttrs) {
            attributeRepository.findByKeyAndValue(attr.getKey(), attr.getValue())
                    .orElseGet(() -> {
                        attributeRepository.save(attr);
                        log.info("🟢 Created attribute: {} = {} (id={})", attr.getKey(), attr.getValue(), attr.getId());
                        return attr;
                    });
        }
    }

    // -----------------------------
    //  SLUG HELPER
    // -----------------------------
    private String generateSlug(String input) {
        if (input == null) return null;
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // bỏ dấu tiếng Việt
                .replaceAll("đ", "d")
                .replaceAll("Đ", "d")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", ""); // loại bỏ dấu '-' ở đầu/cuối
    }
}

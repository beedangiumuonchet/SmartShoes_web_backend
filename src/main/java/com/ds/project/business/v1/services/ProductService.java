package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.*;
import com.ds.project.app_context.repositories.*;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.*;
import com.ds.project.common.entities.dto.response.*;
import com.ds.project.common.interfaces.IProductService;
import com.ds.project.common.mapper.ProductMapper;
import com.ds.project.common.mapper.ProductVariantMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.*;

/**
 * Service for managing Products
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {
    private final CbirService cbirService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final AttributeRepository attributeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final OrderDetailRepository orderDetailRepository;
    private final CartDetailRepository cartDetailRepository;
    private final ProductVariantService productVariantService;
    private final GoogleDriveService googleDriveService;

    /**
     * Create a new Product with variants, images, and attributes
     */

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        try {
            // 🔹 1. Kiểm tra brand + category tồn tại
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // 🔹 2. Kiểm tra trùng tên sản phẩm
            if (productRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
                throw new RuntimeException("Product name already exists");
            }

            // 🔹 3. Sinh slug duy nhất từ name
            String baseSlug = generateSlug(request.getName());
            String slug = baseSlug;
            int counter = 1;
            while (productRepository.findBySlug(slug).isPresent()) {
                slug = baseSlug + "-" + counter++;
            }

            // 🔹 4. Tạo đối tượng Product (chưa gắn quan hệ con)
            Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .brand(brand)
                    .category(category)
                    .slug(slug)
                    .status(Product.Status.valueOf(request.getStatus().toUpperCase()))
                    .build();

            // 🔹 5. Xử lý danh sách Attributes (đi theo Product)
            Set<ProductAttribute> attributes = new HashSet<>();
            if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
                for (ProductAttributeRequest attrReq : request.getAttributes()) {
                    Attribute attribute = attributeRepository.findById(attrReq.getAttributeId())
                            .orElseThrow(() -> new RuntimeException("Attribute not found"));

                    ProductAttribute pa = ProductAttribute.builder()
                            .product(product)
                            .attribute(attribute)
                            .build();

                    attributes.add(pa);
                }
            }
            product.setProductAttributes(attributes);

            // 🔹 6. Xử lý danh sách Variants (mỗi variant có thể có ảnh riêng)
            Set<ProductVariant> variants = new HashSet<>();
            if (request.getVariants() != null && !request.getVariants().isEmpty()) {
                for (ProductVariantRequest variantReq : request.getVariants()) {
                    Color color = colorRepository.findById(variantReq.getColorId())
                            .orElseThrow(() -> new RuntimeException("Color not found"));

                    ProductVariant variant = ProductVariant.builder()
                            .product(product)
                            .color(color)
                            .size(variantReq.getSize())
                            .price(variantReq.getPrice())
                            .stock(variantReq.getStock())
//                            .sku(variantReq.getSku())
                            .build();

                    // 🔹 Kiểm tra và gắn danh sách ảnh cho variant
                    if (variantReq.getImages() != null && !variantReq.getImages().isEmpty()) {
                        long mainCount = variantReq.getImages().stream()
                                .filter(ProductImageRequest::getIsMain)
                                .count();
                        if (mainCount > 1)
                            throw new RuntimeException("Each variant can only have one main image");

                        List<ProductImage> variantImages = new ArrayList<>();
                        for (ProductImageRequest imgReq : variantReq.getImages()) {
                            // ✅ 6.1 gửi file sang Flask để extract embedding
                            List<CbirService.ImageFeatureResult> extracted =
                                    cbirService.extractImagesAndFeatures(imgReq.getFile());

                            System.out.println("File name: " + imgReq.getFile().getOriginalFilename());
                            System.out.println("File size: " + imgReq.getFile().getSize());
                            System.out.println("Extracted: " + extracted);

                            if (extracted.isEmpty())
                                throw new RuntimeException("Failed to extract image embeddings");

                            CbirService.ImageFeatureResult extractedImg = extracted.get(0);

                            // ✅ 6.2 upload ảnh lên storage → nhận url
                            String uploadedUrl = googleDriveService.uploadFile(
                                    imgReq.getFile()
                            );

                            // ✅ 6.3 save image + embedding vào DB

                            Double[] embeddingArray = extractedImg.getFeatures()
                                    .toArray(new Double[0]);

                            ProductImage image = ProductImage.builder()
                                    .url(uploadedUrl)
                                    .isMain(imgReq.getIsMain())
                                    .productVariant(variant)
                                    .embedding(embeddingArray)
                                    .build();

                            ProductImage savedImg = productImageRepository.save(image);

                            log.info("Url: {}", savedImg.getUrl());
//                             ✅ 6.4 push embedding sang Flask cache/RAM
                            cbirService.pushFeatureToFlask(
                                    savedImg.getId(),
                                    savedImg.getProductVariant().getId(),
                                    savedImg.getUrl(),
                                    savedImg.getEmbedding()
                            );

                            variantImages.add(image);
                        }
                        variant.setImages(variantImages);
                    }

                    variants.add(variant);
                }
            }
            product.setVariants(variants);

            // 🔹 7. Lưu toàn bộ product (cascade sang các bảng con)
            Product saved = productRepository.save(product);

            log.info("✅ Created product '{}' with {} variants and {} attributes",
                    saved.getName(),
                    saved.getVariants() != null ? saved.getVariants().size() : 0,
                    saved.getProductAttributes() != null ? saved.getProductAttributes().size() : 0);

            return productMapper.mapToDto(saved);

        } catch (Exception e) {
            log.error("❌ Error while creating product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create product: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        try {
            // 🔹 1. Lấy product hiện tại
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 🔹 2. Kiểm tra brand/category tồn tại
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // 🔹 3. Xử lý đổi tên → đổi slug
            if (!product.getName().equalsIgnoreCase(request.getName())) {
                if (productRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
                    throw new RuntimeException("Product name already exists");
                }

                String baseSlug = generateSlug(request.getName());
                String slug = baseSlug;
                int counter = 1;

                while (productRepository.findBySlug(slug).isPresent() &&
                        !productRepository.findBySlug(slug).get().getId().equals(product.getId())) {
                    slug = baseSlug + "-" + counter++;
                }

                product.setName(request.getName());
                product.setSlug(slug);
            }

            product.setDescription(request.getDescription());
            product.setBrand(brand);
            product.setCategory(category);

            if (request.getStatus() != null) {
                product.setStatus(Product.Status.valueOf(request.getStatus().toUpperCase()));
            }

            // ============================================
            // 🔹 4. UPDATE ATTRIBUTES
            // ============================================
            productAttributeRepository.deleteAll(product.getProductAttributes());

            Set<ProductAttribute> newAttributes = new HashSet<>();
            if (request.getAttributes() != null) {
                for (ProductAttributeRequest attrReq : request.getAttributes()) {
                    Attribute attribute = attributeRepository.findById(attrReq.getAttributeId())
                            .orElseThrow(() -> new RuntimeException("Attribute not found"));

                    ProductAttribute pa = ProductAttribute.builder()
                            .product(product)
                            .attribute(attribute)
                            .build();

                    newAttributes.add(pa);
                }
            }
            product.setProductAttributes(newAttributes);

            // ============================================
            // 🔹 5. UPDATE VARIANTS + IMAGES (THEO LOGIC CREATE)
            // ============================================
            Set<ProductVariant> existingVariants = product.getVariants() != null
                    ? product.getVariants()
                    : new HashSet<>();

            Set<String> requestVariantIds = new HashSet<>();

            for (ProductVariantRequest variantReq : request.getVariants()) {

                ProductVariant variant;

                // 5.1 — UPDATE VARIANT CŨ
                if (variantReq.getId() != null) {
                    variant = existingVariants.stream()
                            .filter(v -> v.getId().equals(variantReq.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantReq.getId()));

                    // cập nhật size/stock/price/color
                    productVariantService.updateVariant(variant.getId(), variantReq);

                }
                // 5.2 — CREATE VARIANT MỚI
                else {
                    ProductVariantResponse created = productVariantService.createVariant(product.getId(), variantReq);
                    variant = productVariantRepository.findById(created.getId())
                            .orElseThrow(() -> new RuntimeException("Variant create failed"));
                    existingVariants.add(variant);
                }

                // ID variant trong request
                requestVariantIds.add(variant.getId());

                // ============================================
                // 🔥 5.3 UPDATE IMAGES (THEO LOGIC CREATE)
                // ============================================
                // 🔥 ẢNH: update thông minh, không xoá hết như trước
                List<ProductImage> currentImages = variant.getImages();

// Nếu null thì tạo list rỗng và gán vào variant 1 lần duy nhất
                if (currentImages == null) {
                    currentImages = new ArrayList<>();
                    variant.setImages(currentImages);
                }

// Tập ID ảnh từ request
                Set<String> requestImageIds = new HashSet<>();

// DANH SÁCH ẢNH MỚI (để add thêm vào currentImages)
                List<ProductImage> newImagesToAdd = new ArrayList<>();

                for (ProductImageRequest imgReq : variantReq.getImages()) {

                    // CASE 1: ảnh cũ — cập nhật isMain
                    if (imgReq.getId() != null) {
                        requestImageIds.add(imgReq.getId());

                        ProductImage oldImg = currentImages.stream()
                                .filter(i -> i.getId().equals(imgReq.getId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Image not found: " + imgReq.getId()));

                        oldImg.setIsMain(imgReq.getIsMain());
                        continue;
                    }

                    // CASE 2: ảnh mới upload
                    if (imgReq.getFile() != null) {

                        List<CbirService.ImageFeatureResult> extracted =
                                cbirService.extractImagesAndFeatures(imgReq.getFile());

                        if (extracted.isEmpty())
                            throw new RuntimeException("Failed to extract embedding");

                        String uploadedUrl = googleDriveService.uploadFile(imgReq.getFile());

                        ProductImage newImg = ProductImage.builder()
                                .url(uploadedUrl)
                                .isMain(imgReq.getIsMain())
                                .productVariant(variant)
                                .embedding(extracted.get(0).getFeatures().toArray(new Double[0]))
                                .build();

                        ProductImage savedImg = productImageRepository.save(newImg);

                        cbirService.pushFeatureToFlask(
                                savedImg.getId(),
                                variant.getId(),
                                savedImg.getUrl(),
                                savedImg.getEmbedding()
                        );

                        newImagesToAdd.add(savedImg);
                    }
                }

// CASE 3: xoá ảnh không còn trong request
                currentImages.removeIf(oldImg -> {
                    if (oldImg.getId() != null && !requestImageIds.contains(oldImg.getId())) {

                        // Xoá bên flask
                        // cbirService.removeFeature(oldImg.getId());

                        productImageRepository.delete(oldImg);
                        return true; // xoá khỏi list
                    }
                    return false;
                });

// Cuối cùng: thêm ảnh mới → vào list cũ
                currentImages.addAll(newImagesToAdd);
// KHÔNG được gọi variant.setImages()



            }

            // ============================================
            // 🔹 6. XÓA VARIANT KHÔNG CÓ TRONG REQUEST
            // ============================================
            existingVariants.removeIf(v -> {
                if (!requestVariantIds.contains(v.getId())) {
                    boolean used = checkVariantUsage(v);
                    return !used;  // true → xóa
                }
                return false;
            });

            product.setVariants(existingVariants);

            // ============================================
            // 🔹 7. SAVE PRODUCT
            // ============================================
            Product saved = productRepository.save(product);

            log.info("✅ Updated product '{}': {} variants, {} attributes",
                    saved.getName(),
                    saved.getVariants().size(),
                    saved.getProductAttributes().size());

            return productMapper.mapToDto(saved);

        } catch (Exception e) {
            log.error("❌ Failed to update product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to update product: " + e.getMessage());
        }
    }


    /**
     * Kiểm tra xem variant có đang được tham chiếu ở nơi khác không
     */
    private boolean checkVariantUsage(ProductVariant variantId) {
        // Ví dụ: kiểm tra trong bảng OrderItem
        boolean usedInOrders = orderDetailRepository.existsByProductVariant(variantId);

        // Kiểm tra thêm các bảng khác nếu cần (CartItem, Inventory,...)
        boolean usedInOtherTables = cartDetailRepository.existsByProductVariant(variantId);

        return usedInOrders || usedInOtherTables;
    }



    /**
     * Get all products with filter and pagination
     */
    @Override
    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        try {
            String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
            String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

            Specification<Product> spec = (root, query, cb) -> {
                query.distinct(true);
                List<Predicate> predicates = new ArrayList<>();

                // JOIN
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.LEFT);
                Join<Product, Brand> brandJoin = root.join("brand", JoinType.LEFT);
                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                Join<Product, ProductAttribute> paJoin = root.join("productAttributes", JoinType.LEFT);
                Join<ProductAttribute, Attribute> attributeJoin = paJoin.join("attribute", JoinType.LEFT);

                // ===========================
                // 1. FILTER Product base fields
                // ===========================
                if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                    String keyword = "%" + filter.getQ().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), keyword),
                            cb.like(cb.lower(root.get("slug")), keyword),
                            cb.like(cb.lower(root.get("description")), keyword)
                    ));
                }

                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }

                if (filter.getCreatedAtFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            root.get("createdAt"), filter.getCreatedAtFrom().atStartOfDay()));
                }

                if (filter.getCreatedAtTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                            root.get("createdAt"), filter.getCreatedAtTo().atTime(23, 59, 59)));
                }

                // ===========================
                // 2. FILTER Brand & Category
                // ===========================
                if (filter.getBrandIds() != null && !filter.getBrandIds().isEmpty()) {
                    predicates.add(brandJoin.get("id").in(filter.getBrandIds()));
                }

                if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                    predicates.add(categoryJoin.get("id").in(filter.getCategoryIds()));
                }

                // ===========================
                // 3. FILTER Variant (price, size, color, stock)
                // ===========================

                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), filter.getMinPrice()));
                }

                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), filter.getMaxPrice()));
                }

                if (Boolean.TRUE.equals(filter.getInStock())) {
                    predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
                } else if (Boolean.FALSE.equals(filter.getInStock())) {
                    predicates.add(cb.equal(variantJoin.get("stock"), 0));
                }

                if (filter.getColorIds() != null && !filter.getColorIds().isEmpty()) {
                    predicates.add(variantJoin.get("color").get("id").in(filter.getColorIds()));
                }

                if (filter.getSizes() != null && !filter.getSizes().isEmpty()) {
                    predicates.add(variantJoin.get("size").in(filter.getSizes()));
                }

                // ===========================
                // 4. FILTER Attributes
                // ===========================
                if (filter.getAttributeIds() != null && !filter.getAttributeIds().isEmpty()) {
                    predicates.add(attributeJoin.get("id").in(filter.getAttributeIds()));
                }

                if (filter.getAttributeKey() != null && !filter.getAttributeKey().isEmpty()) {
                    predicates.add(cb.equal(cb.lower(attributeJoin.get("key")),
                            filter.getAttributeKey().toLowerCase()));
                }

                if (filter.getAttributeValue() != null && !filter.getAttributeValue().isEmpty()) {
                    predicates.add(cb.like(cb.lower(attributeJoin.get("value")),
                            "%" + filter.getAttributeValue().toLowerCase() + "%"));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Page<Product> productPage = productRepository.findAll(spec, pageable);

            return PaginationResponse.<ProductResponse>builder()
                    .content(productPage.getContent().stream()
                            .map(productMapper::mapToDto)
                            .toList())
                    .page(productPage.getNumber())
                    .size(productPage.getSize())
                    .totalElements(productPage.getTotalElements())
                    .totalPages(productPage.getTotalPages())
                    .first(productPage.isFirst())
                    .last(productPage.isLast())
                    .hasNext(productPage.hasNext())
                    .hasPrevious(productPage.hasPrevious())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products: " + e.getMessage());
        }
    }

//    @Override
//    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
//        try {
//            // Sort mặc định
//            String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
//            String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";
//
//            Sort sort = sortDir.equalsIgnoreCase("desc")
//                    ? Sort.by(sortBy).descending()
//                    : Sort.by(sortBy).ascending();
//
//            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
//
//            // === Tạo specification động ===
//            Specification<Product> spec = (root, query, cb) -> {
//                query.distinct(true);
//                List<Predicate> predicates = new ArrayList<>();
//
//
//                // ======= JOIN sang ProductVariant =======
//                // product -> variants
//                var variantJoin = root.join("variants", JoinType.LEFT);
//
//
//                if (filter.getQ() != null && !filter.getQ().isEmpty()) {
//                    String keyword = "%" + filter.getQ().toLowerCase() + "%";
//                    Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
//                    Predicate slugPredicate = cb.like(cb.lower(root.get("slug")), keyword);
//                    Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
//                    predicates.add(cb.or(namePredicate, slugPredicate, descPredicate));
//                }
//
//                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
//                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
//                }
//
//                if (filter.getCreatedAtFrom() != null) {
//                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom().atStartOfDay()));
//                }
//
//                if (filter.getCreatedAtTo() != null) {
//                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo().atTime(23, 59, 59)));
//                }
//
//                if (filter.getMinPrice() != null) {
//                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), filter.getMinPrice()));
//                }
//
//                if (filter.getMaxPrice() != null) {
//                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), filter.getMaxPrice()));
//                }
//
//                if (filter.getMinPrice() != null && filter.getMaxPrice() != null &&
//                        filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
//                    throw new IllegalArgumentException("minPrice không được lớn hơn maxPrice");
//                }
//
//                if (Boolean.TRUE.equals(filter.getInStock())) {
//                    // chỉ lấy sản phẩm có ít nhất 1 variant còn hàng
//                    predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
//                } else if (Boolean.FALSE.equals(filter.getInStock())) {
//                    // chỉ lấy sản phẩm có tất cả variant hết hàng
//                    predicates.add(cb.equal(variantJoin.get("stock"), 0));
//                }
//
//
//
//                return cb.and(predicates.toArray(new Predicate[0]));
//            };
//
//            Page<Product> productPage = productRepository.findAll(spec, pageable);
//
//            return PaginationResponse.<ProductResponse>builder()
//                    .content(productPage.getContent().stream()
//                            .map(productMapper::mapToDto)
//                            .toList())
//                    .page(productPage.getNumber())
//                    .size(productPage.getSize())
//                    .totalElements(productPage.getTotalElements())
//                    .totalPages(productPage.getTotalPages())
//                    .first(productPage.isFirst())
//                    .last(productPage.isLast())
//                    .hasNext(productPage.hasNext())
//                    .hasPrevious(productPage.hasPrevious())
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error fetching products: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to fetch products: " + e.getMessage());
//        }
//    }

    /**
     * Get product detail by id
     */
    @Override
    public ProductResponse getProductById(String id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            return productMapper.mapToDto(product);
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get product: " + e.getMessage());
        }
    }

    /**
     * Get product detail by slug
     */
    @Override
    public ProductResponse getProductBySlug(String slug) {
        try {
            Product product = productRepository.findBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
            return productMapper.mapToDto(product);
        } catch (Exception e) {
            log.error("Error fetching product by slug {}: {}", slug, e.getMessage(), e);
            throw new RuntimeException("Failed to get product by slug: " + e.getMessage());
        }
    }


    /**
     * Get products by Brand ID with filter & pagination
     */
    public PaginationResponse<ProductResponse> getProductsByBrand(String brandId, ProductFilterRequest filter) {
        try {
            if (!brandRepository.existsById(brandId)) {
                throw new RuntimeException("Brand not found with id=" + brandId);
            }

            String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
            String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

            Specification<Product> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Join variants
                var variantJoin = root.join("variants", JoinType.LEFT);

                // Filter by brand
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));

                // Keyword search
                if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                    String keyword = "%" + filter.getQ().toLowerCase() + "%";
                    Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
                    Predicate slugPredicate = cb.like(cb.lower(root.get("slug")), keyword);
                    Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                    predicates.add(cb.or(namePredicate, slugPredicate, descPredicate));
                }

                // Status filter
                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }

                // CreatedAt filter
                if (filter.getCreatedAtFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom().atStartOfDay()));
                }
                if (filter.getCreatedAtTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo().atTime(23, 59, 59)));
                }

                // Price filter
                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), filter.getMaxPrice()));
                }

                if (Boolean.TRUE.equals(filter.getInStock())) {
                    predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
                } else if (Boolean.FALSE.equals(filter.getInStock())) {
                    predicates.add(cb.equal(variantJoin.get("stock"), 0));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Page<Product> productPage = productRepository.findAll(spec, pageable);

            return PaginationResponse.<ProductResponse>builder()
                    .content(productPage.getContent().stream().map(productMapper::mapToDto).toList())
                    .page(productPage.getNumber())
                    .size(productPage.getSize())
                    .totalElements(productPage.getTotalElements())
                    .totalPages(productPage.getTotalPages())
                    .first(productPage.isFirst())
                    .last(productPage.isLast())
                    .hasNext(productPage.hasNext())
                    .hasPrevious(productPage.hasPrevious())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching products by brand {}: {}", brandId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products by brand: " + e.getMessage());
        }
    }

    /**
     * Get products by Category ID with filter & pagination
     */
    public PaginationResponse<ProductResponse> getProductsByCategory(String categoryId, ProductFilterRequest filter) {
        try {
            if (!categoryRepository.existsById(categoryId)) {
                throw new RuntimeException("Category not found with id=" + categoryId);
            }

            String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
            String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

            Specification<Product> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Join variants
                var variantJoin = root.join("variants", JoinType.LEFT);

                // Filter by category
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));

                // Keyword search
                if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                    String keyword = "%" + filter.getQ().toLowerCase() + "%";
                    Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
                    Predicate slugPredicate = cb.like(cb.lower(root.get("slug")), keyword);
                    Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                    predicates.add(cb.or(namePredicate, slugPredicate, descPredicate));
                }

                // Status filter
                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }

                // CreatedAt filter
                if (filter.getCreatedAtFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom().atStartOfDay()));
                }
                if (filter.getCreatedAtTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo().atTime(23, 59, 59)));
                }

                // Price filter
                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), filter.getMaxPrice()));
                }

                if (Boolean.TRUE.equals(filter.getInStock())) {
                    predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
                } else if (Boolean.FALSE.equals(filter.getInStock())) {
                    predicates.add(cb.equal(variantJoin.get("stock"), 0));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Page<Product> productPage = productRepository.findAll(spec, pageable);

            return PaginationResponse.<ProductResponse>builder()
                    .content(productPage.getContent().stream().map(productMapper::mapToDto).toList())
                    .page(productPage.getNumber())
                    .size(productPage.getSize())
                    .totalElements(productPage.getTotalElements())
                    .totalPages(productPage.getTotalPages())
                    .first(productPage.isFirst())
                    .last(productPage.isLast())
                    .hasNext(productPage.hasNext())
                    .hasPrevious(productPage.hasPrevious())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching products by category {}: {}", categoryId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products by category: " + e.getMessage());
        }
    }


    @Transactional
//    public List<ProductImageResponse> searchSimilarImages(MultipartFile file) {
//        JsonNode response = cbirService.searchImage(file);
//
//        List<ProductImageResponse> results = new ArrayList<>();
//
//        for (JsonNode item : response.get("results")) {
//            String imageId = item.has("id") && !item.get("id").isNull()
//                    ? item.get("id").asText()
//                    : null;
//
//            ProductImage productImage = null;
//            if (imageId != null) {
//                try {
//                    productImage = productImageRepository.findById(imageId)
//                            .orElse(null);
//                } catch (Exception e) {
//                    System.out.println("ProductImage not found for ID: " + imageId);
//                }
//            }
//
//            results.add(ProductImageResponse.builder()
//                    .id(productImage != null ? productImage.getId() : null)
//                    .url(productImage != null ? productImage.getUrl() : item.get("imagePath").asText())
//                    .productVariantId(productImage != null && productImage.getProductVariant() != null
//                            ? productImage.getProductVariant().getId()
//                            : null)
////                    .embedding(productImage != null ? productImage.getEmbedding() : null)
//                    .build());
//        }
//
//        return results;
//    }
//    public List<ProductVariantWithProductResponse> searchSimilarImages(MultipartFile file) {
//        // Gọi CBIR service để tìm ảnh tương tự
//        JsonNode response = cbirService.searchImage(file);
//
//        List<ProductVariantWithProductResponse> results = new ArrayList<>();
//
//        for (JsonNode item : response.get("results")) {
//            String imageId = item.has("id") && !item.get("id").isNull()
//                    ? item.get("id").asText()
//                    : null;
//
//            if (imageId == null) continue; // bỏ qua nếu không có id
//
//            ProductImage productImage = null;
//            try {
//                productImage = productImageRepository.findById(imageId).orElse(null);
//            } catch (Exception e) {
//                System.out.println("ProductImage not found for ID: " + imageId);
//            }
//
//            if (productImage != null && productImage.getProductVariant() != null) {
//                String variantId = productImage.getProductVariant().getId();
//
//                // Lấy variant + product
//                ProductVariantWithProductResponse variantWithProduct =
//                        productVariantService.getVariantWithProductById(variantId);
//
//                results.add(variantWithProduct);
//            }
//        }
//
//        return results;
//    }



    // 2️⃣ Sửa hàm searchSimilarImages để trả về danh sách ProductResponse duy nhất
    public List<ProductResponse> searchSimilarImages(MultipartFile file) {
        JsonNode response = cbirService.searchImage(file);
        Set<String> productIds = new LinkedHashSet<>(); // dùng LinkedHashSet để giữ thứ tự, loại trùng

        for (JsonNode item : response.get("results")) {
            String imageId = item.has("id") && !item.get("id").isNull()
                    ? item.get("id").asText()
                    : null;

            if (imageId == null) continue;

            try {
                ProductImage productImage = productImageRepository.findById(imageId).orElse(null);
                if (productImage != null && productImage.getProductVariant() != null) {
                    String productId = getProductIdByVariantId(productImage.getProductVariant().getId());
                    if (productId != null) {
                        productIds.add(productId);
                    }
                }
            } catch (Exception e) {
                log.warn("ProductImage not found for ID: {}", imageId);
            }
        }

        // Lấy product theo productId duy nhất
        List<ProductResponse> products = new ArrayList<>();
        for (String productId : productIds) {
            try {
                ProductResponse product = getProductById(productId);
                products.add(product);
            } catch (Exception e) {
                log.warn("Product not found for ID: {}", productId);
            }
        }

        return products;
    }


    // 1️⃣ Hàm tìm productId từ variantId
    public String getProductIdByVariantId(String variantId) {
        return productVariantRepository.findById(variantId)
                .map(variant -> variant.getProduct().getId())
                .orElse(null);
    }

    private String generateSlug(String input) {
        if (input == null) return null;

        // 🔹 Bỏ dấu tiếng Việt
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 🔹 Thay ký tự đặc biệt thành khoảng trắng, rồi thay khoảng trắng thành '-'
        String slug = withoutAccents.trim()
                .toLowerCase()
                .replaceAll("đ", "d")                // riêng chữ "đ"
                .replaceAll("[^a-z0-9\\s-]", "")     // bỏ ký tự không hợp lệ
                .replaceAll("\\s+", "-")             // thay khoảng trắng bằng '-'
                .replaceAll("-+", "-");              // gộp nhiều dấu '-' liên tiếp

        return slug;
    }


}

package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.*;
import com.ds.project.app_context.repositories.*;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.*;
import com.ds.project.common.entities.dto.response.*;
import com.ds.project.common.interfaces.IProductService;
import com.ds.project.common.mapper.ProductMapper;
import com.ds.project.common.mapper.ProductVariantMapper;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

/**
 * Service for managing Products
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {

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

    /**
     * Create a new Product with variants, images, and attributes
     */
//    @Override
//    public ProductResponse createProduct(ProductRequest request) {
//        try {
//
//            // Kiểm tra brand + category
//            Brand brand = brandRepository.findById(request.getBrandId())
//                    .orElseThrow(() -> new RuntimeException("Brand not found"));
//            Category category = categoryRepository.findById(request.getCategoryId())
//                    .orElseThrow(() -> new RuntimeException("Category not found"));
//
//            // 🔹 Kiểm tra trùng tên sản phẩm
//            if (productRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
//                throw new RuntimeException("Product name already exists");
//            }
//
//            // 🔹 Sinh slug từ name
//            String baseSlug = generateSlug(request.getName());
//            String uniqueSlug = baseSlug;
//            int counter = 1;
//            while (productRepository.findBySlug(uniqueSlug).isPresent()) {
//                uniqueSlug = baseSlug + "-" + counter++;
//            }
//
//            // === Variants ===
//            List<ProductVariant> variants = new ArrayList<>();
//            if (request.getVariants() != null) {
//                for (ProductVariantRequest variantReq : request.getVariants()) {
//                    Color color = colorRepository.findById(variantReq.getColorId())
//                            .orElseThrow(() -> new RuntimeException("Color not found"));
//
//                    ProductVariant variant = productVariantMapper.mapToEntity(variantReq, null, color); // product sẽ gán sau
//
//                    variants.add(variant);
//                }
//            }
//
//            // === Images ===
//            List<ProductImage> images = new ArrayList<>();
//            if (request.getImages() != null) {
//                for (ProductImageRequest imageReq : request.getImages()) {
//                    ProductImage image = ProductImage.builder()
//                            .url(imageReq.getUrl())
//                            .isMain(imageReq.getIsMain())
//                            .build();
//                    images.add(image);
//                }
//            }
//
//            // === Attributes ===
//            List<ProductAttribute> attributes = new ArrayList<>();
//            if (request.getAttributes() != null) {
//                for (ProductAttributeRequest attrReq : request.getAttributes()) {
//                    Attribute attribute = attributeRepository.findById(attrReq.getAttributeId())
//                            .orElseThrow(() -> new RuntimeException("Attribute not found"));
//
//                    ProductAttribute pa = ProductAttribute.builder()
//                            .attribute(attribute)
//                            .build();
//                    attributes.add(pa);
//                }
//            }
//
//            // === Map sang Entity ===
//            Product product = productMapper.mapToEntity(request, brand, category, variants, images, attributes, uniqueSlug);
//
//            // Gán quan hệ ngược
//            product.getVariants().forEach(v -> v.setProduct(product));
//            product.getProductAttributes().forEach(a -> a.setProduct(product));
//
//            Product saved = productRepository.save(product);
//            return productMapper.mapToDto(saved);
//
//        } catch (Exception e) {
//            log.error("Error while creating product: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to create product: " + e.getMessage());
//        }
//    }

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
                    .status(Product.Status.ACTIVE)
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
                            ProductImage image = ProductImage.builder()
                                    .url(imgReq.getUrl())
                                    .isMain(imgReq.getIsMain())
                                    .productVariant(variant)
                                    .build();
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


    /**
     * Get all products with filter and pagination
     */
    @Override
    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        try {
            // Sort mặc định
            String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
            String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

            // === Tạo specification động ===
            Specification<Product> spec = (root, query, cb) -> {

                List<Predicate> predicates = new ArrayList<>();


                // ======= JOIN sang ProductVariant =======
                // product -> variants
                var variantJoin = root.join("variants", JoinType.LEFT);


                if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                    String keyword = "%" + filter.getQ().toLowerCase() + "%";
                    Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
                    Predicate slugPredicate = cb.like(cb.lower(root.get("slug")), keyword);
                    Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                    predicates.add(cb.or(namePredicate, slugPredicate, descPredicate));
                }

                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }

                if (filter.getCreatedAtFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom().atStartOfDay()));
                }

                if (filter.getCreatedAtTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo().atTime(23, 59, 59)));
                }

                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), filter.getMinPrice()));
                }

                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), filter.getMaxPrice()));
                }

                if (filter.getMinPrice() != null && filter.getMaxPrice() != null &&
                        filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
                    throw new IllegalArgumentException("minPrice không được lớn hơn maxPrice");
                }

                if (Boolean.TRUE.equals(filter.getInStock())) {
                    // chỉ lấy sản phẩm có ít nhất 1 variant còn hàng
                    predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
                } else if (Boolean.FALSE.equals(filter.getInStock())) {
                    // chỉ lấy sản phẩm có tất cả variant hết hàng
                    predicates.add(cb.equal(variantJoin.get("stock"), 0));
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

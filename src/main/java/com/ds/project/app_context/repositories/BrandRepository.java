package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Brand;
import com.ds.project.app_context.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String>, JpaSpecificationExecutor<Brand> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    Optional<Brand> findBySlug(String slug);

    Optional<Brand> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    boolean existsBySlugAndIdNot(String slug, String id);

}

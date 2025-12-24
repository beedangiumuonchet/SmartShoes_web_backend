package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Brand;
import com.ds.project.app_context.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> , JpaSpecificationExecutor<Category> {

    Optional<Category> findByName(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsBySlug(String slug);
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
    boolean existsBySlugAndIdNot(String slug, String id);

}

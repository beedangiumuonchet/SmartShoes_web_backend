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

    Optional<Brand> findByNameIgnoreCase(String name);

}

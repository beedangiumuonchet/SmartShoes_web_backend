package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, String>, JpaSpecificationExecutor<Color> {

    Optional<Color> findByName(String name);

    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Color> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

}

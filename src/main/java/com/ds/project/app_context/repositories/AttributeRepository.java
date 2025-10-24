package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, String>, JpaSpecificationExecutor<Attribute> {

    Optional<Attribute> findByKey(String key);

    boolean existsByKey(String key);
    Optional<Attribute> findByKeyAndValue(String key, String value);
    boolean existsByKeyIgnoreCaseAndValueIgnoreCase(String key, String value);

}

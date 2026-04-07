package com.meritcap.repository;

import com.meritcap.model.DocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeEntityRepository extends JpaRepository<DocumentTypeEntity, Long> {
    List<DocumentTypeEntity> findByIsActiveTrueOrderByNameAsc();
    Optional<DocumentTypeEntity> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByNameIgnoreCase(String name);
}

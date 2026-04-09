package com.meritcap.repository;

import com.meritcap.model.DocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeEntityRepository extends JpaRepository<DocumentTypeEntity, Long> {
    List<DocumentTypeEntity> findByIsActiveTrueOrderByNameAsc();
    Optional<DocumentTypeEntity> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find a soft-deleted record by code.
     * Bypasses the @Where(clause = "is_deleted = false") filter.
     */
    @Query(value = "SELECT * FROM document_types WHERE code = :code AND is_deleted = true", nativeQuery = true)
    Optional<DocumentTypeEntity> findDeletedByCodeIgnoreCase(@Param("code") String code);

    /**
     * Restore a soft-deleted record by setting is_deleted = false.
     */
    @Modifying
    @Query(value = "UPDATE document_types SET is_deleted = false WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Long id);
}

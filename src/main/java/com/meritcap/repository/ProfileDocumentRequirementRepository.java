package com.meritcap.repository;

import com.meritcap.model.ProfileDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileDocumentRequirementRepository extends JpaRepository<ProfileDocumentRequirement, Long> {
    List<ProfileDocumentRequirement> findAllByOrderByDisplayOrderAsc();
    Optional<ProfileDocumentRequirement> findByDocumentTypeId(Long documentTypeId);
    boolean existsByDocumentTypeId(Long documentTypeId);

    /**
     * Find a soft-deleted record by document type.
     * Bypasses the @Where(clause = "is_deleted = false") filter.
     */
    @Query(value = "SELECT * FROM profile_document_requirements WHERE document_type_id = :documentTypeId AND is_deleted = true", nativeQuery = true)
    Optional<ProfileDocumentRequirement> findDeletedByDocumentTypeId(@Param("documentTypeId") Long documentTypeId);

    /**
     * Restore a soft-deleted record by setting is_deleted = false.
     */
    @Modifying
    @Query(value = "UPDATE profile_document_requirements SET is_deleted = false WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Long id);
}

package com.meritcap.repository;

import com.meritcap.model.CountryDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CountryDocumentRequirementRepository extends JpaRepository<CountryDocumentRequirement, Long> {
    List<CountryDocumentRequirement> findByCountryIdOrderByDisplayOrderAsc(Long countryId);
    Optional<CountryDocumentRequirement> findByCountryIdAndDocumentTypeId(Long countryId, Long documentTypeId);
    boolean existsByCountryIdAndDocumentTypeId(Long countryId, Long documentTypeId);

    @Modifying
    @Query("UPDATE CountryDocumentRequirement r SET r.isDeleted = true WHERE r.country.id = :countryId")
    void softDeleteAllByCountryId(@Param("countryId") Long countryId);

    /**
     * Find a soft-deleted record by country and document type.
     * Bypasses the @Where(clause = "is_deleted = false") filter.
     */
    @Query(value = "SELECT * FROM country_document_requirements WHERE country_id = :countryId AND document_type_id = :documentTypeId AND is_deleted = true", nativeQuery = true)
    Optional<CountryDocumentRequirement> findDeletedByCountryIdAndDocumentTypeId(
            @Param("countryId") Long countryId,
            @Param("documentTypeId") Long documentTypeId);

    /**
     * Restore a soft-deleted record by setting is_deleted = false.
     */
    @Modifying
    @Query(value = "UPDATE country_document_requirements SET is_deleted = false WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Long id);
}

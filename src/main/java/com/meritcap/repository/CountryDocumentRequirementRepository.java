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
}

package com.meritcap.repository;

import com.meritcap.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByReferenceTypeAndReferenceIdAndDocumentTypeAndIsDeletedFalse(String referenceType, Long referenceId, String documentType);
    List<Document> findAllByReferenceTypeAndReferenceIdAndIsDeletedFalse(String referenceType, Long referenceId);

    List<Document> findAllByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    @Modifying
    @Query("DELETE FROM Document d WHERE d.referenceType = :referenceType AND d.referenceId = :referenceId")
    void deleteAllByReferenceTypeAndReferenceId(@Param("referenceType") String referenceType,
                                                @Param("referenceId") Long referenceId);
}

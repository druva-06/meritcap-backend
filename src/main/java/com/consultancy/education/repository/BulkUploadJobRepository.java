package com.consultancy.education.repository;

import com.consultancy.education.model.BulkUploadJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface BulkUploadJobRepository extends JpaRepository<BulkUploadJob, Long> {

    /**
     * Increment processed_records by delta and set updated_at.
     * Returns number of rows updated (should be 1 if id exists).
     */
    @Modifying
    @Transactional
    @Query("UPDATE BulkUploadJob b SET b.processedRecords = COALESCE(b.processedRecords, 0) + :delta, b.updatedAt = :updatedAt WHERE b.id = :id")
    int incrementProcessedRecords(@Param("id") Long id,
                                  @Param("delta") int delta,
                                  @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update status and optionally error message. Use null for err if no error text is required.
     */
    @Modifying
    @Transactional
    @Query("UPDATE BulkUploadJob b SET b.status = :status, b.updatedAt = :updatedAt, b.errorMessage = :err WHERE b.id = :id")
    int updateStatusAndError(@Param("id") Long id,
                             @Param("status") BulkUploadJob.Status status,
                             @Param("err") String err,
                             @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Set totalRecords and updated_at for the job.
     */
    @Modifying
    @Transactional
    @Query("UPDATE BulkUploadJob b SET b.totalRecords = :total, b.updatedAt = :updatedAt WHERE b.id = :id")
    int updateTotalRecords(@Param("id") Long id,
                           @Param("total") int total,
                           @Param("updatedAt") LocalDateTime updatedAt);
}

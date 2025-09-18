package com.consultancy.education.repository;

import com.consultancy.education.model.BulkUploadError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkUploadErrorRepository extends JpaRepository<BulkUploadError, Long> {
    // additional query methods can be added later if required
}


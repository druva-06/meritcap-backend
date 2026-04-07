package com.meritcap.repository;

import com.meritcap.model.ProfileDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileDocumentRequirementRepository extends JpaRepository<ProfileDocumentRequirement, Long> {
    List<ProfileDocumentRequirement> findAllByOrderByDisplayOrderAsc();
    Optional<ProfileDocumentRequirement> findByDocumentTypeId(Long documentTypeId);
    boolean existsByDocumentTypeId(Long documentTypeId);
}

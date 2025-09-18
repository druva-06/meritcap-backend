package com.consultancy.education.repository;

import com.consultancy.education.model.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CollegeRepository extends JpaRepository<College, Long> {
    boolean existsBySlug(String slug);
    boolean existsByCampusCode(String campusCode);
    List<College> findByCampusCodeIn(Set<String> campusCodes);
    java.util.Optional<College> findByCampusCode(String campusCode);
}

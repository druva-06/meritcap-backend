package com.meritcap.repository;

import com.meritcap.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByIsActiveTrueOrderByNameAsc();
    Optional<Country> findByNameIgnoreCase(String name);
    Optional<Country> findByCodeIgnoreCase(String code);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCase(String code);
}

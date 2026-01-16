package com.consultancy.education.repository;

import com.consultancy.education.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Optional<Role> findByNameIgnoreCase(String name);

    List<Role> findByIsActiveTrue();

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :roleId")
    Role findByIdWithPermissions(@Param("roleId") Long roleId);
}

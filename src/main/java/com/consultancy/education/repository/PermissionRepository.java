package com.consultancy.education.repository;

import com.consultancy.education.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    Optional<Permission> findByNameIgnoreCase(String name);

    List<Permission> findByIsActiveTrue();

    List<Permission> findByCategory(String category);

    List<Permission> findByCategoryAndIsActiveTrue(String category);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    Set<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT p FROM Permission p JOIN p.users u WHERE u.id = :userId")
    Set<Permission> findAdditionalPermissionsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Permission p " +
            "LEFT JOIN p.roles r " +
            "LEFT JOIN p.users u " +
            "WHERE (r.id = :roleId) OR (u.id = :userId)")
    Set<Permission> findAllPermissionsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}

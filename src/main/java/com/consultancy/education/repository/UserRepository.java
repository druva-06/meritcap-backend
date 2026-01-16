package com.consultancy.education.repository;

import com.consultancy.education.model.Role;
import com.consultancy.education.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    User findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    User findByUsername(String username);

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.additionalPermissions WHERE u.id = :userId")
    User findByIdWithPermissions(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
}

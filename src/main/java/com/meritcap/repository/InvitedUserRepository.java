package com.meritcap.repository;

import com.meritcap.model.InvitedUser;
import com.meritcap.model.InvitedUser.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitedUserRepository extends JpaRepository<InvitedUser, Long> {

    // Find by email
    Optional<InvitedUser> findByEmail(String email);

    // Find by invitation token
    Optional<InvitedUser> findByInvitationToken(String invitationToken);

    // Check if email already invited
    boolean existsByEmail(String email);

    // Find by status
    List<InvitedUser> findByStatus(InvitationStatus status);

    Page<InvitedUser> findByStatus(InvitationStatus status, Pageable pageable);

    // Find by invited by user
    List<InvitedUser> findByInvitedById(Long invitedById);

    void deleteAllByUserId(Long userId);

    // Find pending invitations that are expired
    @Query("SELECT i FROM InvitedUser i WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    List<InvitedUser> findExpiredPendingInvitations(@Param("now") LocalDateTime now);

    // Update status for expired invitations (scheduled job can call this)
    @Modifying
    @Query("UPDATE InvitedUser i SET i.status = 'EXPIRED', i.updatedAt = :now WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    int markExpiredInvitations(@Param("now") LocalDateTime now);

    // Get all invitations with pagination and search
    @Query("SELECT i FROM InvitedUser i WHERE " +
            "LOWER(i.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<InvitedUser> searchInvitations(@Param("search") String search, Pageable pageable);

    // Count by status
    long countByStatus(InvitationStatus status);

    // Find invitations created in date range
    @Query("SELECT i FROM InvitedUser i WHERE i.invitedAt BETWEEN :startDate AND :endDate")
    List<InvitedUser> findInvitationsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find by role
    @Query("SELECT i FROM InvitedUser i WHERE i.role.id = :roleId")
    List<InvitedUser> findByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("UPDATE InvitedUser i SET i.user = null, i.updatedAt = :now WHERE i.user.id = :userId")
    int clearUserReference(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM InvitedUser i WHERE i.invitedBy.id = :userId")
    void deleteAllByInvitedById(@Param("userId") Long userId);
}

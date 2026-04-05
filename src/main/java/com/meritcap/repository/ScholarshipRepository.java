package com.meritcap.repository;

import com.meritcap.model.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    @Modifying
    @Query("DELETE FROM Scholarship s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

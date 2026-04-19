package com.meritcap.repository;

import com.meritcap.model.RoundRobinState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRobinStateRepository extends JpaRepository<RoundRobinState, Long> {

    Optional<RoundRobinState> findByCounselorId(Long counselorId);

    List<RoundRobinState> findAllByOrderByLastAssignedAtAscAssignmentCountAsc();
}

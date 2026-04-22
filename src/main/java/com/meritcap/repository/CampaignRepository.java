package com.meritcap.repository;

import com.meritcap.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByName(String name);

    boolean existsByName(String name);

    List<Campaign> findAllByOrderByCreatedAtDesc();
}

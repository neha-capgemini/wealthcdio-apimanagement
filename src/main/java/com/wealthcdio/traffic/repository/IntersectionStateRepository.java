package com.wealthcdio.traffic.repository;

import com.wealthcdio.traffic.model.IntersectionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository for IntersectionState entity
 */
@Repository
public interface IntersectionStateRepository extends JpaRepository<IntersectionState, Long> {
    
    /**
     * Find intersection state by intersection ID
     */
    Optional<IntersectionState> findByIntersectionId(String intersectionId);

    /**
     * Check if intersection exists
     */
    boolean existsByIntersectionId(String intersectionId);

    /**
     * Find intersection state with pessimistic lock for concurrency control
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IntersectionState> findAndLockByIntersectionId(String intersectionId);
}

package com.wealthcdio.traffic.repository;

import com.wealthcdio.traffic.model.Direction;
import com.wealthcdio.traffic.model.TimingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TimingHistory entity
 */
@Repository
public interface TimingHistoryRepository extends JpaRepository<TimingHistory, Long> {

    /**
     * Find timing history for an intersection
     */
    List<TimingHistory> findByIntersectionIdOrderByStartTimeDesc(String intersectionId);

    /**
     * Find timing history for a specific direction
     */
    List<TimingHistory> findByIntersectionIdAndDirectionOrderByStartTimeDesc(String intersectionId, Direction direction);

    /**
     * Get total duration for a direction and status
     */
    @Query("SELECT SUM(th.durationSeconds) FROM TimingHistory th WHERE th.intersectionId = :intersectionId AND th.direction = :direction AND th.status = :status")
    Long getTotalDurationForDirectionAndStatus(@Param("intersectionId") String intersectionId,
                                              @Param("direction") Direction direction,
                                              @Param("status") String status);
}

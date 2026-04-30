package com.wealthcdio.traffic.repository;

import com.wealthcdio.traffic.model.Direction;
import com.wealthcdio.traffic.model.LightStatus;
import com.wealthcdio.traffic.model.TrafficLight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TrafficLight entity
 */
@Repository
public interface TrafficLightRepository extends JpaRepository<TrafficLight, Long> {
    
    /**
     * Find traffic light by intersection and direction
     */
    Optional<TrafficLight> findByIntersectionIdAndDirection(String intersectionId, Direction direction);

    /**
     * Find all lights for an intersection
     */
    List<TrafficLight> findByIntersectionId(String intersectionId);

    /**
     * Find all green lights
     */
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.status = :status AND tl.intersectionId = :intersectionId")
    List<TrafficLight> findByStatusAndIntersectionId(@Param("status") LightStatus status, 
                                                      @Param("intersectionId") String intersectionId);

    /**
     * Find green light for specific intersection
     */
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.intersectionId = :intersectionId AND tl.status = 'GREEN'")
    Optional<TrafficLight> findCurrentGreenLight(@Param("intersectionId") String intersectionId);
}


package com.wealthcdio.traffic.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity representing the current state of an entire intersection
 */
@Entity
@Table(name = "intersection_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntersectionState implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intersection_id", nullable = false, unique = true)
    private String intersectionId;

    @Column(name = "current_green_direction")
    @Enumerated(EnumType.STRING)
    private Direction currentGreenDirection;

    @Column(name = "is_paused", nullable = false)
    private Boolean isPaused;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "total_elapsed_time")
    private Long totalElapsedTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private Map<Direction, TrafficLight> lightsByDirection = new ConcurrentHashMap<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isPaused = false;
        totalElapsedTime = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if intersection is currently operational
     */
    public boolean isOperational() {
        return !isPaused;
    }

    /**
     * Get all directions
     */
    public Direction[] getAllDirections() {
        return Direction.values();
    }
}


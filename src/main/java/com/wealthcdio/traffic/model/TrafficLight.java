package com.wealthcdio.traffic.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing a single traffic light
 */
@Entity
@Table(name = "traffic_lights", uniqueConstraints = 
    {@UniqueConstraint(columnNames = {"intersection_id", "direction"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficLight implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intersection_id", nullable = false)
    private String intersectionId;

    @Column(name = "direction", nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LightStatus status;

    @Column(name = "remaining_time")
    private Integer remainingTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "duration_green")
    private Integer durationGreen;

    @Column(name = "duration_yellow")
    private Integer durationYellow;

    @Column(name = "duration_red")
    private Integer durationRed;

    @Column(name = "total_elapsed_time")
    private Long totalElapsedTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        totalElapsedTime = 0L;
        durationGreen = 30;
        durationYellow = 5;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate total time for this light (green + yellow + red)
     */
    public Integer getTotalCycleDuration() {
        return durationGreen + durationYellow + durationRed;
    }

    /**
     * Check if light is expired
     */
    public boolean isExpired() {
        return remainingTime != null && remainingTime <= 0;
    }
}


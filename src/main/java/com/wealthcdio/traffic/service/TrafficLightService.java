package com.wealthcdio.traffic.service;

import com.wealthcdio.traffic.exception.InvalidStateTransitionException;
import com.wealthcdio.traffic.exception.TrafficException;
import com.wealthcdio.traffic.model.*;
import com.wealthcdio.traffic.repository.TimingHistoryRepository;
import com.wealthcdio.traffic.repository.TrafficLightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing individual traffic lights
 */
@Slf4j
@Service
@Transactional
public class TrafficLightService {

    @Autowired
    private TrafficLightRepository trafficLightRepository;

    @Autowired
    private TimingHistoryRepository timingHistoryRepository;

    @Autowired
    private TimingManager timingManager;

    /**
     * Initialize traffic lights for an intersection
     */
    public void initializeLightsForIntersection(String intersectionId) {
        try {
            Direction[] directions = Direction.getDefaultSequence();
            
            for (Direction direction : directions) {
                Optional<TrafficLight> existing = 
                    trafficLightRepository.findByIntersectionIdAndDirection(intersectionId, direction);
                
                if (existing.isEmpty()) {
                    TrafficLight light = TrafficLight.builder()
                        .intersectionId(intersectionId)
                        .direction(direction)
                        .status(LightStatus.RED)
                        .remainingTime(0)
                        .durationGreen(30)
                        .durationYellow(5)
                        .durationRed(90)
                        .totalElapsedTime(0L)
                        .build();
                    
                    trafficLightRepository.save(light);
                    log.info("Initialized traffic light for {} - {}", intersectionId, direction);
                }
            }
        } catch (Exception e) {
            log.error("Error initializing lights for intersection {}", intersectionId, e);
            throw new TrafficException("INITIALIZATION_ERROR", 
                "Failed to initialize lights: " + e.getMessage());
        }
    }

    /**
     * Get traffic light by intersection and direction
     */
    public TrafficLight getTrafficLight(String intersectionId, Direction direction) {
        TrafficLight light = trafficLightRepository.findByIntersectionIdAndDirection(intersectionId, direction)
            .orElseThrow(() -> new TrafficException("LIGHT_NOT_FOUND",
                String.format("Traffic light not found for %s - %s", intersectionId, direction)));
        return light;
    }

    /**
     * Get all lights for an intersection
     */
    public List<TrafficLight> getAllLights(String intersectionId) {
        return trafficLightRepository.findByIntersectionId(intersectionId);
    }

    /**
     * Get current green light for intersection
     */
    public Optional<TrafficLight> getCurrentGreenLight(String intersectionId) {
        return trafficLightRepository.findCurrentGreenLight(intersectionId);
    }

    /**
     * Change light status with validation
     */
    public TrafficLight changeStatus(String intersectionId, Direction direction, LightStatus newStatus) {
        TrafficLight light = getTrafficLight(intersectionId, direction);
        
        // Validate state transition
        validateStateTransition(light.getStatus(), newStatus);
        
        // Save timing history for previous status
        if (light.getStartTime() != null) {
            TimingHistory history = TimingHistory.builder()
                .intersectionId(intersectionId)
                .direction(direction)
                .status(light.getStatus())
                .startTime(light.getStartTime())
                .endTime(LocalDateTime.now())
                .durationSeconds(Duration.between(light.getStartTime(), LocalDateTime.now()).getSeconds())
                .build();
            timingHistoryRepository.save(history);
        }
        
        light.setStatus(newStatus);
        light.setStartTime(LocalDateTime.now());
        light.setRemainingTime(timingManager.getExpectedDuration(newStatus, light));
        
        TrafficLight saved = trafficLightRepository.save(light);
        
        log.info("Changed light status: {} - {} from {} to {}", 
                intersectionId, direction, light.getStatus(), newStatus);
        
        return saved;
    }

    /**
     * Validate state transitions
     * Valid transitions:
     * RED -> GREEN
     * GREEN -> YELLOW
     * YELLOW -> RED
     * RED -> RED (no change)
     */
    private void validateStateTransition(LightStatus currentStatus, LightStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case RED -> newStatus == LightStatus.RED || newStatus == LightStatus.GREEN;
            case GREEN -> newStatus == LightStatus.YELLOW;
            case YELLOW -> newStatus == LightStatus.RED;
        };
        
        if (!isValid) {
            throw new InvalidStateTransitionException(currentStatus, newStatus);
        }
    }

    /**
     * Update remaining time for all lights
     */
    public void updateAllRemainingTimes(String intersectionId) {
        List<TrafficLight> lights = getAllLights(intersectionId);
        for (TrafficLight light : lights) {
            int remaining = timingManager.calculateRemainingTime(light);
            light.setRemainingTime(remaining);
            trafficLightRepository.save(light);
        }
    }

    /**
     * Pause all lights
     */
    public void pauseAllLights(String intersectionId) {
        List<TrafficLight> lights = getAllLights(intersectionId);
        for (TrafficLight light : lights) {
            timingManager.pauseTimer(light);
            trafficLightRepository.save(light);
        }
        log.info("All lights paused for intersection {}", intersectionId);
    }

    /**
     * Resume all lights
     */
    public void resumeAllLights(String intersectionId) {
        List<TrafficLight> lights = getAllLights(intersectionId);
        for (TrafficLight light : lights) {
            if (light.getStatus() != LightStatus.RED || light.getRemainingTime() > 0) {
                timingManager.resumeTimer(light);
                trafficLightRepository.save(light);
            }
        }
        log.info("All lights resumed for intersection {}", intersectionId);
    }

}

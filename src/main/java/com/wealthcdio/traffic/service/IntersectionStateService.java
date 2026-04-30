package com.wealthcdio.traffic.service;

import com.wealthcdio.traffic.exception.ConflictingDirectionsException;
import com.wealthcdio.traffic.exception.TrafficException;
import com.wealthcdio.traffic.model.*;
import com.wealthcdio.traffic.repository.IntersectionStateRepository;
import com.wealthcdio.traffic.repository.TrafficLightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing entire intersection states
 */
@Slf4j
@Service
@Transactional
public class IntersectionStateService {

    @Autowired
    private IntersectionStateRepository intersectionStateRepository;

    @Autowired
    private TrafficLightRepository trafficLightRepository;

    @Autowired
    private TrafficLightService trafficLightService;

    /**
     * Initialize intersection - creates all lights in default sequence
     */
    @Transactional
    public IntersectionState initializeIntersection(String intersectionId) {
        try {
            Optional<IntersectionState> existing = 
                intersectionStateRepository.findByIntersectionId(intersectionId);
            
            if (existing.isPresent()) {
                log.warn("Intersection {} already initialized", intersectionId);
                return existing.get();
            }

            IntersectionState state = IntersectionState.builder()
                .intersectionId(intersectionId)
                .currentGreenDirection(Direction.NORTH)
                .isPaused(false)
                .totalElapsedTime(0L)
                .build();

            IntersectionState saved = intersectionStateRepository.save(state);

            trafficLightService.initializeLightsForIntersection(intersectionId);

            setGreenLight(intersectionId, Direction.NORTH);

            log.info("Initialized intersection: {}", intersectionId);

            return saved;
        } catch (Exception e) {
            log.error("Error initializing intersection {}", intersectionId, e);
            throw new TrafficException("INIT_ERROR", 
                "Failed to initialize intersection: " + e.getMessage());
        }
    }

    /**
     * Get intersection state
     */
    public IntersectionState getIntersectionState(String intersectionId) {
        IntersectionState state = intersectionStateRepository.findByIntersectionId(intersectionId)
            .orElseThrow(() -> new TrafficException("INTERSECTION_NOT_FOUND",
                String.format("Intersection %s not found", intersectionId)));
        return state;
    }

    /**
     * Get current state with all light details
     */
    public IntersectionState getCurrentState(String intersectionId) {
        IntersectionState state = getIntersectionState(intersectionId);
        
        Map<Direction, TrafficLight> lightsByDirection = new HashMap<>();
        List<TrafficLight> lights = trafficLightRepository.findByIntersectionId(intersectionId);
        
        for (TrafficLight light : lights) {
            lightsByDirection.put(light.getDirection(), light);
        }
        
        state.setLightsByDirection(lightsByDirection);
        return state;
    }

    /**
     * Set green light for specific direction with validation
     */
    @Transactional
    public void setGreenLight(String intersectionId, Direction newGreenDirection) {
        // Use database-level locking for better concurrency
        IntersectionState state = intersectionStateRepository.findAndLockByIntersectionId(intersectionId)
            .orElseThrow(() -> new TrafficException("INTERSECTION_NOT_FOUND",
                String.format("Intersection %s not found", intersectionId)));

        Direction currentGreen = state.getCurrentGreenDirection();

        validateNoConflictingGreenLights(intersectionId, newGreenDirection);

        if (!newGreenDirection.equals(currentGreen) && currentGreen != null) {
            transitionFromGreenToRed(intersectionId, currentGreen);
        }

        trafficLightService.changeStatus(intersectionId, newGreenDirection, LightStatus.GREEN);
        
        state.setCurrentGreenDirection(newGreenDirection);
        state.setUpdatedAt(LocalDateTime.now());
        
        intersectionStateRepository.save(state);

        log.info("Green light set for {} - {}", intersectionId, newGreenDirection);
    }

    /**
     * Validate that no conflicting directions are green
     */
    private void validateNoConflictingGreenLights(String intersectionId, Direction newDirection) {
        Set<Direction> conflicting = Direction.getConflictingDirections(newDirection);
        
        for (Direction dir : conflicting) {
            Optional<TrafficLight> light = trafficLightRepository
                .findByIntersectionIdAndDirection(intersectionId, dir);
            
            if (light.isPresent() && light.get().getStatus() == LightStatus.GREEN) {
                throw new ConflictingDirectionsException(newDirection, dir);
            }
        }
    }

    /**
     * Transition green light to yellow then red
     */
    private void transitionFromGreenToRed(String intersectionId, Direction direction) {
        try {
            trafficLightService.changeStatus(intersectionId, direction, LightStatus.YELLOW);
            trafficLightService.changeStatus(intersectionId, direction, LightStatus.RED);
        } catch (Exception e) {
            log.warn("Error transitioning {} from green to red", direction, e);
        }
    }

    /**
     * Pause traffic lights
     */
    @Transactional
    public IntersectionState pauseTraffic(String intersectionId) {
        IntersectionState state = getIntersectionState(intersectionId);
        
        if (state.getIsPaused()) {
            throw new TrafficException("ALREADY_PAUSED", 
                "Intersection " + intersectionId + " is already paused");
        }

        state.setIsPaused(true);
        state.setPausedAt(LocalDateTime.now());
        
        trafficLightService.pauseAllLights(intersectionId);
        
        IntersectionState saved = intersectionStateRepository.save(state);
        
        log.info("Traffic paused for intersection {}", intersectionId);
        return saved;
    }

    /**
     * Resume traffic lights
     */
    @Transactional
    public IntersectionState resumeTraffic(String intersectionId) {
        IntersectionState state = getIntersectionState(intersectionId);
        
        if (!state.getIsPaused()) {
            throw new TrafficException("NOT_PAUSED", 
                "Intersection " + intersectionId + " is not paused");
        }

        state.setIsPaused(false);
        state.setPausedAt(null);
        
        trafficLightService.resumeAllLights(intersectionId);
        
        IntersectionState saved = intersectionStateRepository.save(state);
        
        log.info("Traffic resumed for intersection {}", intersectionId);
        return saved;
    }

}

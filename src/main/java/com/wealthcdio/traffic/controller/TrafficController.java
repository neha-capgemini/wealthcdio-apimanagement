package com.wealthcdio.traffic.controller;

import com.wealthcdio.traffic.model.IntersectionState;
import com.wealthcdio.traffic.model.TrafficLight;
import com.wealthcdio.traffic.service.IntersectionStateService;
import com.wealthcdio.traffic.service.TrafficLightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Traffic Light Management
 */
@Slf4j
@RestController
@RequestMapping("/traffic")
@Tag(name = "Traffic Management", description = "APIs for managing traffic lights at intersections")
public class TrafficController {

    @Autowired
    private IntersectionStateService intersectionStateService;

    @Autowired
    private TrafficLightService trafficLightService;

    /**
     * Initialize intersection
     */
    @PostMapping("/intersection/{intersectionId}/initialize")
    @Operation(summary = "Initialize intersection", description = "Initialize a new intersection with default traffic lights")
    public ResponseEntity<IntersectionState> initializeIntersection(@PathVariable String intersectionId) {
        try {
            IntersectionState state = intersectionStateService.initializeIntersection(intersectionId);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            log.error("Error initializing intersection {}", intersectionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current intersection state
     */
    @GetMapping("/intersection/{intersectionId}/state")
    @Operation(summary = "Get intersection state", description = "Retrieve current state of all traffic lights at an intersection")
    public ResponseEntity<IntersectionState> getIntersectionState(@PathVariable String intersectionId) {
        try {
            IntersectionState state = intersectionStateService.getCurrentState(intersectionId);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            log.error("Error getting intersection state for {}", intersectionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all traffic lights for an intersection
     */
    @GetMapping("/intersection/{intersectionId}/lights")
    @Operation(summary = "Get traffic lights", description = "Retrieve all traffic lights for an intersection")
    public ResponseEntity<List<TrafficLight>> getTrafficLights(@PathVariable String intersectionId) {
        try {
            List<TrafficLight> lights = trafficLightService.getAllLights(intersectionId);
            return ResponseEntity.ok(lights);
        } catch (Exception e) {
            log.error("Error getting traffic lights for {}", intersectionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get specific traffic light
     */
    @GetMapping("/intersection/{intersectionId}/light/{direction}")
    @Operation(summary = "Get traffic light", description = "Retrieve a specific traffic light by direction")
    public ResponseEntity<TrafficLight> getTrafficLight(@PathVariable String intersectionId,
                                                        @PathVariable String direction) {
        try {
            TrafficLight light = trafficLightService.getTrafficLight(intersectionId,
                com.wealthcdio.traffic.model.Direction.valueOf(direction.toUpperCase()));
            return ResponseEntity.ok(light);
        } catch (Exception e) {
            log.error("Error getting traffic light for {} - {}", intersectionId, direction, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Change light status
     */
    @PutMapping("/intersection/{intersectionId}/light/{direction}/status/{status}")
    @Operation(summary = "Change light status", description = "Change the status of a specific traffic light")
    public ResponseEntity<TrafficLight> changeLightStatus(@PathVariable String intersectionId,
                                                          @PathVariable String direction,
                                                          @PathVariable String status) {
        try {
            log.info("Attempting to change light status: intersection={}, direction={}, status={}", 
                     intersectionId, direction, status);
            
            com.wealthcdio.traffic.model.Direction dir = com.wealthcdio.traffic.model.Direction.valueOf(direction.toUpperCase());
            com.wealthcdio.traffic.model.LightStatus lightStatus = com.wealthcdio.traffic.model.LightStatus.valueOf(status.toUpperCase());
            
            TrafficLight light = trafficLightService.changeStatus(intersectionId, dir, lightStatus);
            log.info("Successfully changed light status for {}", intersectionId);
            return ResponseEntity.ok(light);
        } catch (IllegalArgumentException e) {
            log.error("Invalid enum value - direction: {}, status: {}", direction, status, e);
            throw new TrafficException("INVALID_ENUM_VALUE", 
                "Invalid direction or status. Direction must be one of [NORTH, SOUTH, EAST, WEST]. Status must be one of [RED, YELLOW, GREEN]");
        } catch (Exception e) {
            log.error("Error changing light status for {} - {} to {}", intersectionId, direction, status, e);
            throw new TrafficException("CHANGE_STATUS_ERROR", e.getMessage());
        }
    }

    /**
     * Set green light for direction
     */
    @PutMapping("/intersection/{intersectionId}/green/{direction}")
    @Operation(summary = "Set green light", description = "Set green light for a specific direction with validation")
    public ResponseEntity<Void> setGreenLight(@PathVariable String intersectionId,
                                              @PathVariable String direction) {
        try {
            com.wealthcdio.traffic.model.Direction dir = com.wealthcdio.traffic.model.Direction.valueOf(direction.toUpperCase());
            intersectionStateService.setGreenLight(intersectionId, dir);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting green light for {} - {}", intersectionId, direction, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Pause traffic
     */
    @PutMapping("/intersection/{intersectionId}/pause")
    @Operation(summary = "Pause traffic", description = "Pause all traffic lights at an intersection")
    public ResponseEntity<IntersectionState> pauseTraffic(@PathVariable String intersectionId) {
        try {
            IntersectionState state = intersectionStateService.pauseTraffic(intersectionId);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            log.error("Error pausing traffic for {}", intersectionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resume traffic
     */
    @PutMapping("/intersection/{intersectionId}/resume")
    @Operation(summary = "Resume traffic", description = "Resume all traffic lights at an intersection")
    public ResponseEntity<IntersectionState> resumeTraffic(@PathVariable String intersectionId) {
        try {
            IntersectionState state = intersectionStateService.resumeTraffic(intersectionId);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            log.error("Error resuming traffic for {}", intersectionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update remaining times
     */
    @PutMapping("/intersection/{intersectionId}/update-times")
    @Operation(summary = "Update remaining times", description = "Update remaining time for all lights")
    public ResponseEntity<Void> updateRemainingTimes(@PathVariable String intersectionId) {
        try {
            trafficLightService.updateAllRemainingTimes(intersectionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating remaining times for {}", intersectionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

}

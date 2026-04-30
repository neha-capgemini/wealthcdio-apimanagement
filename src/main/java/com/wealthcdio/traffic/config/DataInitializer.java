package com.wealthcdio.traffic.config;

import com.wealthcdio.traffic.model.*;
import com.wealthcdio.traffic.repository.IntersectionStateRepository;
import com.wealthcdio.traffic.repository.TrafficLightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialize dummy data on application startup
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TrafficLightRepository trafficLightRepository;

    @Autowired
    private IntersectionStateRepository intersectionStateRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("========== Initializing dummy traffic data ==========");

        initializeIntersection("MAIN_5TH");
        initializeIntersection("BROADWAY_PARK");
        initializeIntersection("DOWNTOWN_CENTER");

        log.info("========== Dummy data initialization completed! ==========");
    }

    private void initializeIntersection(String intersectionId) {
        if (intersectionStateRepository.existsByIntersectionId(intersectionId)) {
            log.info("Intersection {} already exists, skipping", intersectionId);
            return;
        }

        IntersectionState intersectionState = IntersectionState.builder()
            .intersectionId(intersectionId)
            .currentGreenDirection(Direction.NORTH)
            .isPaused(false)
            .totalElapsedTime(0L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        IntersectionState savedState = intersectionStateRepository.save(intersectionState);
        log.info("✓ Created intersection: {}", intersectionId);

        for (Direction direction : Direction.getDefaultSequence()) {
            TrafficLight light = TrafficLight.builder()
                .intersectionId(intersectionId)
                .direction(direction)
                .status(direction == Direction.NORTH ? LightStatus.GREEN : LightStatus.RED)
                .remainingTime(direction == Direction.NORTH ? 30 : 90)
                .startTime(LocalDateTime.now())
                .durationGreen(30)
                .durationYellow(5)
                .durationRed(90)
                .totalElapsedTime(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            trafficLightRepository.save(light);
            log.info("  ✓ Created light: {} - {} [{}]", intersectionId, direction, light.getStatus());
        }
    }
}


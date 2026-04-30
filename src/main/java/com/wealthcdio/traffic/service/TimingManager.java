package com.wealthcdio.traffic.service;

import com.wealthcdio.traffic.model.LightStatus;
import com.wealthcdio.traffic.model.TrafficLight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Manages timing and countdown for traffic lights
 */
@Slf4j
@Component
public class TimingManager {

    /**
     * Update remaining time based on elapsed time
     */
    public Integer calculateRemainingTime(TrafficLight light) {
        if (light.getStartTime() == null) {
            return light.getRemainingTime();
        }

        LocalDateTime now = LocalDateTime.now();
        long elapsedSeconds = ChronoUnit.SECONDS.between(light.getStartTime(), now);
        
        Integer expectedDuration = getExpectedDuration(light.getStatus(), light);
        Integer remaining = Math.max(0, (int) (expectedDuration - elapsedSeconds));
        
        return remaining;
    }

    /**
     * Get expected duration for current status
     */
    public Integer getExpectedDuration(LightStatus status, TrafficLight light) {
        return switch (status) {
            case GREEN -> light.getDurationGreen();
            case YELLOW -> light.getDurationYellow();
            case RED -> light.getDurationRed();
        };
    }

    /**
     * Check if light time has expired
     */
    public boolean isTimeExpired(TrafficLight light) {
        return calculateRemainingTime(light) <= 0;
    }

    /**
     * Reset timer for light
     */
    public void resetTimer(TrafficLight light) {
        light.setStartTime(LocalDateTime.now());
        light.setRemainingTime(getExpectedDuration(light.getStatus(), light));
        log.info("Timer reset for {} - {} with remaining time: {} seconds", 
                light.getIntersectionId(), light.getDirection(), light.getRemainingTime());
    }

    /**
     * Pause timer - calculate elapsed time
     */
    public void pauseTimer(TrafficLight light) {
        if (light.getStartTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long elapsedMillis = ChronoUnit.MILLIS.between(light.getStartTime(), now);
            light.setTotalElapsedTime(light.getTotalElapsedTime() + elapsedMillis);
            light.setStartTime(null);
        }
    }

    /**
     * Resume timer from pause
     */
    public void resumeTimer(TrafficLight light) {
        light.setStartTime(LocalDateTime.now());
        log.info("Timer resumed for {} - {}", light.getIntersectionId(), light.getDirection());
    }

    /**
     * Format remaining time as MM:SS
     */
    public String formatTime(Integer seconds) {
        if (seconds == null || seconds < 0) {
            return "00:00";
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}


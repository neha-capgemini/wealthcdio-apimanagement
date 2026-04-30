package com.wealthcdio.traffic.model;

/**
 * Enumeration for traffic light status
 */
public enum LightStatus {
    RED("Red Light"),
    YELLOW("Yellow Light"),
    GREEN("Green Light");

    private final String description;

    LightStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


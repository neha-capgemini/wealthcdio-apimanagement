package com.wealthcdio.traffic.model;

import java.util.Set;

/**
 * Enumeration for traffic directions at intersection
 */
public enum Direction {
    NORTH("North"),
    SOUTH("South"),
    EAST("East"),
    WEST("West");

    private final String displayName;

    Direction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the opposing direction
     */
    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /**
     * Get all directions that conflict with this direction
     */
    public static Set<Direction> getConflictingDirections(Direction direction) {
        return Set.of(direction.getOpposite());
    }

    /**
     * Get default sequence order
     */
    public static Direction[] getDefaultSequence() {
        return new Direction[]{NORTH, SOUTH, EAST, WEST};
    }
}


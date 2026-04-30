package com.wealthcdio.traffic.exception;

import com.wealthcdio.traffic.model.Direction;

/**
 * Exception thrown when attempting to set conflicting directions to green
 */
public class ConflictingDirectionsException extends TrafficException {
    
    private Direction direction1;
    private Direction direction2;

    public ConflictingDirectionsException(Direction dir1, Direction dir2) {
        super("CONFLICTING_LIGHTS", 
            String.format("Cannot have %s and %s both green simultaneously", 
            dir1.getDisplayName(), dir2.getDisplayName()));
        this.direction1 = dir1;
        this.direction2 = dir2;
    }

    public Direction getDirection1() {
        return direction1;
    }

    public Direction getDirection2() {
        return direction2;
    }
}


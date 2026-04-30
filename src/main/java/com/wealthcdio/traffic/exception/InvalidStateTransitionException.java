package com.wealthcdio.traffic.exception;

import com.wealthcdio.traffic.model.LightStatus;

/**
 * Exception thrown for invalid light state transitions
 */
public class InvalidStateTransitionException extends TrafficException {
    
    private LightStatus fromStatus;
    private LightStatus toStatus;

    public InvalidStateTransitionException(LightStatus from, LightStatus to) {
        super("INVALID_STATE_TRANSITION",
            String.format("Cannot transition from %s to %s", from, to));
        this.fromStatus = from;
        this.toStatus = to;
    }

    public LightStatus getFromStatus() {
        return fromStatus;
    }

    public LightStatus getToStatus() {
        return toStatus;
    }
}


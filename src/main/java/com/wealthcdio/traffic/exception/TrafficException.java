package com.wealthcdio.traffic.exception;

/**
 * Base exception for traffic management system
 */
public class TrafficException extends RuntimeException {
    
    private String errorCode;

    public TrafficException(String message) {
        super(message);
        this.errorCode = "TRAFFIC_ERROR";
    }

    public TrafficException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TrafficException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TRAFFIC_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}


package org.example.ratelimitjztcommon.excepition;

public class ExecuteFunctionException extends RuntimeException {
    public ExecuteFunctionException(String message, Throwable cause) {
        super(message,cause);
    }
}

package org.example.ratelimitjztcommon.excepition;

import org.example.ratelimitjztcommon.enums.RateLimitTypeEnum;

public class RateLimitException extends RuntimeException {
    private final RateLimitTypeEnum rateLimitTypeEnum;

    public RateLimitException(String message,RateLimitTypeEnum rateLimitTypeEnum) {
        super(message);
        this.rateLimitTypeEnum = rateLimitTypeEnum;
    }

    public RateLimitTypeEnum getRateLimitTypeEnum(){
        return rateLimitTypeEnum;
    }
}

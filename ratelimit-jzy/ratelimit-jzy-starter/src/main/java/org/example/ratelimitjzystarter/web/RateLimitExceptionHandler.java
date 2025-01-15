package org.example.ratelimitjzystarter.web;


import org.example.ratelimitjztcommon.constant.Constant;
import org.example.ratelimitjztcommon.excepition.RateLimitException;
import org.example.ratelimitjztcommon.model.Result;
import org.example.ratelimitjzystarter.config.RateLimitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ConditionalOnProperty(prefix = Constant.RATELIMIT_PROPERTIES,name = "exception.enable",havingValue = "true",matchIfMissing = true)
public class RateLimitExceptionHandler {
    private final RateLimitProperties rateLimitProperties;
    public RateLimitExceptionHandler(RateLimitProperties rateLimitProperties){
        this.rateLimitProperties = rateLimitProperties;
    }

    @ExceptionHandler(value = RateLimitException.class)
    @ResponseBody
    public Result exceptionHandler(RateLimitException e) {
        Result result = new Result();
        result.setAllow(false);
        result.setDesc(rateLimitProperties.getResponseBody());
        result.setCode(rateLimitProperties.getCode());
        return result;
    }
}

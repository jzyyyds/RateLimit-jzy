package org.example.ratelimitjzytest.controller;

import org.example.ratelimitjztcommon.enums.RateLimitTypeEnum;
import org.example.ratelimitjzycore.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    @RateLimit(rateLimitType = RateLimitTypeEnum.COUNTER , key = "test", rate = 10, time = 1,fallbackFunction = "getFallback")
    public String testLimit(@RequestParam String name) {
        return "hello";
    }

    @GetMapping("/testWindow")
    @RateLimit(rateLimitType = RateLimitTypeEnum.TIME_WINDOW , key = "test_window",capacity = 50, time = 5)
    public String testLimitByTime() {
        return "hello_window";
    }

    @GetMapping("/test/leaky_buckey")
    @RateLimit(rateLimitType = RateLimitTypeEnum.LEAKY_BUCKET,key = "test_leaky_buckey",capacity = 50,rate = 10)
    public String testLeakyBuckey() {
        return "hello_leaky_bucket";
    }

    @GetMapping("/test/token_bucket")
    @RateLimit(rateLimitType = RateLimitTypeEnum.TOKEN_BUCKET,key = "test_token_bucket",capacity = 50,rate = 10)
    public String testTokenBucket() {
        return "hello_token_bucket";
    }

    public String getFallback(@RequestParam String name) {
        return "Too Many Requests"+ name;
    }
}

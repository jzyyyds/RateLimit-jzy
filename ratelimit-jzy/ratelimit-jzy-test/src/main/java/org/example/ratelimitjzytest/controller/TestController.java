package org.example.ratelimitjzytest.controller;

import org.example.ratelimitjztcommon.enums.RateLimitTypeEnum;
import org.example.ratelimitjzycore.annotation.RateLimit;
import org.example.ratelimitjzytest.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @RequestMapping("/test")
    @RateLimit(rateLimitType = RateLimitTypeEnum.COUNTER,rateExpression = "${rateSpel.key.rate}",timeExpression = "${rateSpel.key.time}")
    public String testLimit(@RequestBody User user) {
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

package org.example.ratelimitjzycore.service.impl;

import org.example.ratelimitjztcommon.model.RateLimitRule;
import org.example.ratelimitjzycore.service.RateLimitService;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class LeakyBuckeyRateLimitServiceImpl implements RateLimitService {
    private final RScript rScript;
    public LeakyBuckeyRateLimitServiceImpl(RedissonClient redissonClient) {
        this.rScript = redissonClient.getScript(LongCodec.INSTANCE);
    }

    @Override
    public boolean isAllow(RateLimitRule rateLimitRule) {
        String script = getScript();
        //TODO 时间戳的key先暂时使用存储容量的key来代替，后续会更换，需要保持唯一
        long now = System.currentTimeMillis();
        List<Object> keyLists = Arrays.asList(rateLimitRule.getKey(), rateLimitRule.getKey()+"1");
        //Instant.now().getEpochSecond()
        Long isAllow = rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.INTEGER, keyLists, rateLimitRule.getCapacity(), rateLimitRule.getRate(), now/1000);
        return isAllow == 1;
    }

    @Override
    public String getScript() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/leaky_bucket.lua");
        String luaScript = "";
        try {
            luaScript = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return luaScript;
    }
}

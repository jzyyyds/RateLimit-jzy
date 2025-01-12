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

public class TokenRateLimitServiceImpl implements RateLimitService {
    private final RScript rScript;
    public TokenRateLimitServiceImpl(RedissonClient redissonClient) {
        this.rScript = redissonClient.getScript(LongCodec.INSTANCE);
    }
    @Override
    public String getScript() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/token_bucket.lua");
        String luaScript = "";
        try {
            luaScript = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return luaScript;
    }

    @Override
    public boolean isAllow(RateLimitRule rateLimitRule) {
        String script = getScript();
        //TODO 时间戳的key先暂时使用存储容量的key来代替，后续会更换，需要保持唯一
        long now = System.currentTimeMillis();
        Long cur = now/1000;
        System.out.println("cur:"+cur);
        List<Object> keyLists = Arrays.asList(rateLimitRule.getKey(), rateLimitRule.getKey()+"1");
        Long isAllow = rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.INTEGER, keyLists, rateLimitRule.getCapacity(), rateLimitRule.getRate(), cur);
        return isAllow == 1;
    }
}

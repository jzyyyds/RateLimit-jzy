package org.example.ratelimitjzycore.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.ratelimitjztcommon.model.RateLimitRule;
import org.example.ratelimitjzycore.service.RateLimitService;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
public class WindowRateLimitServiceImpl implements RateLimitService {
    private final RScript rScript;

    public WindowRateLimitServiceImpl(RedissonClient redissonClient){
        this.rScript = redissonClient.getScript(LongCodec.INSTANCE);
    }

    @Override
    public boolean isAllow(RateLimitRule rateLimitRule) {
        String script = getScript();
        long now = System.currentTimeMillis();
        //TODO 此时使用唯一ID是为了避免如果使用时间的话，此时再高并发的情况下，可以会导致zset下的menber重复，导致数据丢失，此时先写死，后续可以优化成
        //TODO Leaf来生成
        List<Object> keyList = Collections.singletonList(rateLimitRule.getKey());
        Long isAllow = rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.INTEGER,keyList,rateLimitRule.getCapacity(),rateLimitRule.getTime()*1000,now,now);
        return isAllow==1;
    }

    @Override
    public String getScript() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/window.lua");
        String luaScript = "";
        try {
            luaScript = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return luaScript;
    }
}

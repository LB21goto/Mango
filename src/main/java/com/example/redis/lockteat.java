package com.example.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class lockteat {
    @Bean
    public DefaultRedisScript<Long> seatLockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
                "if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then return 0 end " +
                        "if redis.call('sismember', KEYS[1], ARGV[1]) == 1 then return 0 end " +
                        "redis.call('sadd', KEYS[1], ARGV[1]); return 1;"
        );
        script.setResultType(Long.class);
        return script;
    }
}

NEW_FILE_CODE
        package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StockInitializer implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) {
        String stockKey = "stock:1001";

        Boolean exists = stringRedisTemplate.hasKey(stockKey);
        if (exists == null || !exists) {
            stringRedisTemplate.opsForValue().set(stockKey, "100");
            log.info("初始化库存成功 -> {}: 100", stockKey);
        } else {
            log.info("库存已存在 -> {}: {}", stockKey, stringRedisTemplate.opsForValue().get(stockKey));
        }

        // 清空之前的座位数据，确保测试环境干净
        String bitmapKey = "seat:bitmap:1";
        String hashKey = "seat:hash:1";
        stringRedisTemplate.delete(bitmapKey);
        stringRedisTemplate.delete(hashKey);
        log.info("已清空座位缓存数据");
    }
}
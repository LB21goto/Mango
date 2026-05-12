package com.example.DrawStrategy;

import com.example.entity.Prize;
import com.example.entity.Activity;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("physicalAsyncDispatcher")
public class PhysicalAsyncDispatcher implements AwardDispatcher {

    private static final String QUEUE_KEY = "lottery:delivery:queue";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PhysicalAsyncDispatcher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getName() { return "physicalAsyncDispatcher"; }

    @Override
    public DeliverResult deliver(Long userId, Prize prize, Activity activity, String requestId) {
        try {
            DeliveryMessage msg = new DeliveryMessage(userId, prize.getId(), activity.getId(), requestId, prize.getMeta());
            String json = objectMapper.writeValueAsString(msg);

            // 幂等保障：SETNX 锁定 requestId，防止重复 enqueue（可选）
            String lockKey = "lottery:deliver:request:" + requestId;
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, "1");
            if (Boolean.FALSE.equals(ok)) {
                return new DeliverResult(DeliverResult.Status.QUEUED, "已存在待处理任务");
            }
            redisTemplate.expire(lockKey, java.time.Duration.ofDays(1));

            redisTemplate.opsForList().rightPush(QUEUE_KEY, json);
            return new DeliverResult(DeliverResult.Status.QUEUED, "已入队等待发货");
        } catch (Exception ex) {
            return new DeliverResult(DeliverResult.Status.FAILED, "入队失败: " + ex.getMessage());
        }
    }

    // 内部消息结构
    public static class DeliveryMessage {
        public Long userId;
        public Long prizeId;
        public Long activityId;
        public String requestId;
        public Object meta; // 可放地址/SKU/特殊字段

        public DeliveryMessage(Long userId, Long prizeId, Long activityId, String requestId, Object meta) {
            this.userId = userId; this.prizeId = prizeId; this.activityId = activityId; this.requestId = requestId; this.meta = meta;
        }
        // getters/setters or public fields ok
    }
}

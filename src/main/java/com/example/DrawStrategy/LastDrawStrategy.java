package com.example.DrawStrategy;

import com.example.entity.Activity;
import com.example.entity.Prize;
import com.example.mapper.PrizeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Slf4j
@Service("listDrawStrategy")
public class LastDrawStrategy implements DrawStrategy{

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource(name = "lottery.lua")
    private DefaultRedisScript<Long> drawLuaScript;

    @Autowired
    private PrizeMapper prizeMapper; // 请替换为你项目的 mapper 名称

    @Override
    public DrawResult executedraw(DrawContext context) {
        // 4. Lua 抽奖（原子扣减库存 + 频控）
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }

        Long activityId = context.getActivityId();
        Activity activity = context.getActivity();
        Long userId = context.getUserId();
        String requestId = "1"; // 请确保 DrawContext 有该方法，若没有需要加

        if (activity == null) {
            log.warn("activity is null for activityId={} requestId={}", activityId, requestId);
            return DrawResult.forbidden("活动不存在或已下线");
        }

        // 构造 Redis keys（使用当前策略的 buildRedisKeys）
        List<String> keys = this.buildRedisKeys(activity, userId);

        String argvUserId = String.valueOf(userId);
        String argvRequestId = requestId == null ? "" : requestId;
        String argvActivityId = String.valueOf(activityId);
        String argvExtra = "{}";

        Long luaResult;
        try {
            luaResult = redisTemplate.execute(drawLuaScript, keys, argvUserId, argvRequestId, argvActivityId, argvExtra);
        } catch (DataAccessException ex) {
            log.error("调用 Redis Lua 脚本异常 activityId={} userId={} requestId={}", activityId, userId, requestId, ex);
            throw new DrawException("抽奖系统忙，请稍后重试");
        }

        if (luaResult == null) {
            log.error("抽奖脚本返回 null activityId={} userId={} requestId={}", activityId, userId, requestId);
            throw new DrawException("抽奖失败，请稍后重试");
        }

        log.info("Lua 脚本返回: {} activityId={} userId={} requestId={}", luaResult, activityId, userId, requestId);

        if (luaResult == 0L) {
            return DrawResult.noStock("活动奖品已售罄");
        } else if (luaResult > 0L) {
            Long prizeId = luaResult;
            Prize prize = prizeMapper.findById(prizeId);
            if (prize == null) {
                log.error("抽中 prizeId={} 但数据库中不存在 activityId={} userId={} requestId={}", prizeId, activityId, userId, requestId);
                // 可考虑把 prizeId 写入补偿表或 push 到人工处理队列
                throw new DrawException("抽奖异常，请联系客服");
            }

            // 初始状态由上层或发奖流程决定
            if (prize.isVirtual()) {
                log.info("虚拟奖品即时发放 userId={} prizeId={} requestId={}", userId, prizeId, requestId);
            } else {
                log.info("实物奖品，等待异步发货 userId={} prizeId={} requestId={}", userId, prizeId, requestId);
            }
            return DrawResult.success(prize);
        } else {
            log.error("未知 lua 返回码: {} activityId={} userId={} requestId={}", luaResult, activityId, userId, requestId);
            throw new DrawException("抽奖失败（未知错误）");
        }
    }

    @Override
    public List<String> buildRedisKeys(Activity activity, Long userId) {
        return DrawStrategy.super.buildRedisKeys(activity, userId);
    }
}

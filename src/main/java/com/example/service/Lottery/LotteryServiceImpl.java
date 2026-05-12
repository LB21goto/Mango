package com.example.service.Lottery;
import com.example.DrawStrategy.*;
import com.example.entity.Activity;
import com.example.entity.Prize;
import com.example.mapper.ActivityMapper;
import com.example.mapper.PrizeMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.util.*;

@Service
public class LotteryServiceImpl implements LotteryService {

    private static final Logger logger = LoggerFactory.getLogger(LotteryServiceImpl.class);

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private PrizeMapper prizeMapper;

    @Resource
    private AwardDispatchFactory awardDispatchFactory;

    @Resource
    private DrawStrategyFactory drawStrategyFactory;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource(name = "lottery.lua")
    private DefaultRedisScript<Long> drawLuaScript;

    @Transactional(rollbackFor = Exception.class)
    public DrawResult draw(Long userId, Long activityId, String clientRequestId) {
        if (userId == null || activityId == null) {
            throw new IllegalArgumentException("userId/activityId cannot be null");
        }

        String requestId = (clientRequestId == null || clientRequestId.isEmpty()) ? UUID.randomUUID().toString() : clientRequestId;
        logger.info("开始抽奖 userId={} activityId={} requestId={}", userId, activityId, requestId);

//        // 1. 读取活动并做基础校验
        Activity activity = activityMapper.findById(activityId);
//        if (activity == null) {
//            logger.warn("抽奖失败： activity not found, activityId={}", activityId);
//            throw new DrawException("活动不存在");
//        }
//
//        // 2. 资格校验：只检查状态和时间窗（状态由 XXL-Job 扫描更新）
//        LocalDateTime now = LocalDateTime.now();
//        if (activity.getStatus() == null || !activity.getStatus().equalsIgnoreCase("ACTIVE")
//                || now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
//            logger.warn("抽奖失败： activity not active, activityId={}, status={}", activityId, activity.getStatus());
//            return DrawResult.forbidden("活动未生效或已结束");
//        }



        // 2. 组装上下文（把当前环境下的所有有用信息全塞进去）
        DrawContext context = new DrawContext(userId, activityId, clientRequestId, activity);
        // 如果需要，还可以查库补充信息塞进 context
        // context.setUserVipLevel(userService.getVipLevel(userId));

        // 3. 选策略
        DrawStrategy strategy = drawStrategyFactory.create(activity);

        // 4. 执行！无脑传 context，不管底层是哪种策略
// ... existing code ...
        // 抽中后
        DrawResult result = strategy.executedraw(context);
        String prizeType = result.getType();
        Prize prize = result.getPrize();
        // 取发奖器并发奖
            AwardDispatcher dispatcher = awardDispatchFactory.getDispatcherFor(prize, activity);
            if (dispatcher == null) {
                // fallback: 标记为 PENDING 并人工处理
            } else {
                DeliverResult dr = dispatcher.deliver(userId, prize, activity, requestId);
                // 根据 dr.status 更新记录（DELIVERED -> 已发放，QUEUED -> 已入队，FAILED -> 标记失败并人工介入）
            }

        return result;
    }

//    private void recordUserDraw(Long userId, Long activityId, Long prizeId, String requestId, String status, String remark) {
//        try {
//            UserDrawRecord rec = new UserDrawRecord();
//            rec.setUserId(userId);
//            rec.setActivityId(activityId);
//            rec.setPrizeId(prizeId);
//            rec.setRequestId(requestId);
//            rec.setStatus(status);
//            rec.setRemark(remark);
//            rec.setCreateTime(LocalDateTime.now());
//
//            UserDrawRecord exist = userDrawRecordMapper.findByRequestId(requestId);
//            if (exist != null) {
//                logger.info("重复请求，忽略重复插入 requestId={}", requestId);
//                return;
//            }
//            userDrawRecordMapper.insert(rec);
//        } catch (Exception ex) {
//            logger.error("记录抽奖历史失败 userId={} activityId={} prizeId={} requestId={}",
//                    userId, activityId, prizeId, requestId, ex);
//            throw new DrawException("记录抽奖结果失败，请联系客服");
//        }
//    }


    private String buildActivityPrizePoolKey(Long activityId) {
        return "lottery:activity:" + activityId + ":prize_pool";
    }

    public Boolean initPrizePool(Long activityId) {
        List<Prize> prizes = prizeMapper.findByActivityId(activityId);
        if (prizes == null || prizes.isEmpty()) {
            logger.warn("活动 {} 没有配置奖品", activityId);
            return false;
        }

        String poolKey = buildActivityPrizePoolKey(activityId);

        redisTemplate.delete(poolKey);

        List<String> prizeIds = new ArrayList<>();
        for (Prize prize : prizes) {
            for (int i = 0; i < prize.getTotalInventory(); i++) {
                prizeIds.add(String.valueOf(prize.getId()));
            }
        }

        Collections.shuffle(prizeIds);

        redisTemplate.opsForList().rightPushAll(poolKey, prizeIds);

        logger.info("活动 {} 奖品池初始化完成，共 {} 个奖品", activityId, prizeIds.size());
        return true;
    }

}


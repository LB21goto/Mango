package com.example.DrawStrategy;

import com.example.entity.Activity;
import com.example.entity.Prize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

public interface DrawStrategy {
    /**
     * 执行抽奖逻辑
     * @param context 该活动下的奖品列表（通常已按权重准备好）
     * @return 抽中的奖品
     */



    DrawResult executedraw(DrawContext context);

    default List<String> buildRedisKeys(Activity activity, Long userId) {
        if (activity == null || activity.getId() == null) {
            throw new IllegalArgumentException("Activity 或 activityId 不能为空");
        }

        String prizePoolKey = "lottery:activity:" + activity.getId() + ":prize_pool";

        return Collections.singletonList(prizePoolKey);
    }
}
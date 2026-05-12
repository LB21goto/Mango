package com.example.DrawStrategy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
public class DrawStrategyFactory {

    @Resource
    private Map<String, DrawStrategy> strategyMap;

    /**
     * 根据活动信息选择合适的抽奖策略
     * @param activity 活动信息
     * @return 对应的抽奖策略
     */
    public DrawStrategy create(com.example.entity.Activity activity) {
        if (activity == null) {
            return strategyMap.getOrDefault("defaultDrawStrategy", null);
        }

        String strategyBeanName = activity.getStrategyType() + "DrawStrategy";
        return strategyMap.getOrDefault(strategyBeanName, strategyMap.get("defaultDrawStrategy"));
    }
}

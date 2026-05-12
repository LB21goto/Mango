package com.example.DrawStrategy;

import com.example.DrawStrategy.AwardDispatcher;
import com.example.entity.Activity;
import com.example.entity.Prize;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 简单工厂：基于 prize 元信息（或 activity）选择合适 dispatcher。
 * 注：在 Spring 中通过 @Component 注入 Map<String, AwardDispatcher>，
 * key = beanName，value = bean 实例。
 */
@Component
public class AwardDispatchFactory {

    @Resource
    private Map<String, AwardDispatcher> dispatcherMap;

    /**
     * 选择 dispatcher 的简单策略（可根据 prize metadata/活动配置扩展）
     */
    public AwardDispatcher getDispatcherFor(Prize prize, Activity activity) {
        if (prize == null) {
            return dispatcherMap.getOrDefault("defaultDispatcher", null);
        }

        // 优先使用 prize 上的 deliveryType 字段（示例字段名，按实际替换）
        String deliveryType = null;
        try { deliveryType = prize.getDeliveryType(); } catch (Exception ignored) {}

        if (deliveryType != null && !deliveryType.isBlank()) {
            // map key 约定：deliveryType + "Dispatcher"，或直接 register beanName = deliveryType
            String beanName = deliveryType + "Dispatcher";
            AwardDispatcher d = dispatcherMap.get(beanName);
            if (d != null) return d;
        }

        // 根据是否虚拟优先选择
        if (prize.isVirtual()) {
            AwardDispatcher d = dispatcherMap.get("virtualImmediateDispatcher");
            if (d != null) return d;
        } else {
            AwardDispatcher d = dispatcherMap.get("physicalAsyncDispatcher");
            if (d != null) return d;
        }

        // fallback
        return dispatcherMap.getOrDefault("defaultDispatcher", null);
    }
}

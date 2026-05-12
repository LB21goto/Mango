package com.example.DrawStrategy;


import com.example.entity.Activity;
import lombok.Data;

@Data
public class DrawContext {
    private Long userId;
    private Long activityId;
    private String clientRequestId;

    // 策略可能用到的扩展字段
    private Activity activity; // 携带完整的活动配置
    private Integer userVipLevel; // 假设大转盘需要用到

    // 构造器（或者用 Builder 模式）
    public DrawContext(Long userId, Long activityId, String clientRequestId, Activity activity) {
        this.userId = userId;
        this.activityId = activityId;
        this.clientRequestId = clientRequestId;
        this.activity = activity;
    }
}

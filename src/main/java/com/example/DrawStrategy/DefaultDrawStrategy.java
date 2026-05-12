package com.example.DrawStrategy;

import com.example.entity.Activity;
import com.example.entity.Prize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("defaultDrawStrategy")
public class DefaultDrawStrategy implements DrawStrategy {

    @Override
    public DrawResult executedraw(DrawContext context) {
        System.out.println("执行默认抽奖策略，活动ID=" + context.getActivityId() + " 用户ID=" + context.getUserId());
        return null;
    }
}

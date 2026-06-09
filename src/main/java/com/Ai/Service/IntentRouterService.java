package com.Ai;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentRouterService {

    // ===== 时间类关键词 =====
    private static final List<String> TIME_KEYS = List.of(
            "几点", "时间", "几号", "星期几", "今天几号", "现在几点", "当前时间", "日期", "今天是几号"
    );

    // ===== 闲聊类关键词 =====
    private static final List<String> CHAT_KEYS = List.of(
            "你好", "在吗", "你是谁", "介绍一下你自己", "谢谢", "哈哈", "晚安", "早上好", "中午好", "晚上好"
    );

    // ===== 修改类关键词 =====
    private static final List<String> MODIFY_KEYS = List.of(
            "修改", "更新", "删除", "新增", "编辑", "改成", "更改", "调整", "变更", "替换"
    );

    // ===== 弹药 / 武器类关键词 =====
    private static final List<String> AMMO_KEYS = List.of(
            "弹药", "弹容量", "载弹量", "口径", "武器", "枪械", "装填", "弹匣", "子弹", "火力"
    );

    public RouteType classify(String message) {
        if (message == null || message.isBlank()) {
            return RouteType.UNKNOWN;
        }

        String text = message.trim();

        // 1. 时间类：优先级最高
        if (containsAny(text, TIME_KEYS)) {
            return RouteType.TIME;
        }

        // 2. 闲聊类
        if (containsAny(text, CHAT_KEYS)) {
            return RouteType.CHAT;
        }

        // 3. 修改类
        if (containsAny(text, MODIFY_KEYS)) {
            return RouteType.MODIFY;
        }

        // 4. 弹药 / 武器类
        if (containsAny(text, AMMO_KEYS)) {
            return RouteType.AMMO;
        }

        // 5. 默认走知识库查询
        return RouteType.RAG_QUERY;
    }

    private boolean containsAny(String text, List<String> keys) {
        for (String key : keys) {
            if (text.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
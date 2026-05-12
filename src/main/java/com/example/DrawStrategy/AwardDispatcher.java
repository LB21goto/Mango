package com.example.DrawStrategy;

import com.example.entity.Prize;
import com.example.entity.Activity;

public interface AwardDispatcher {
    /**
     * 发奖主方法。实现需保证幂等（基于 requestId 或 DB 唯一索引）。
     * @param userId 用户
     * @param prize 抽中奖品对象（包含类型/渠道/isVirtual/metadata）
     * @param activity 活动信息
     * @param requestId 幂等/请求 id
     * @return DeliverResult 表示发放结果（同步或已入队）
     */
    DeliverResult deliver(Long userId, Prize prize, Activity activity, String requestId);

    /**
     * 返回 dispatcher 的标识（便于工厂注册/日志）
     */
    String getName();
}

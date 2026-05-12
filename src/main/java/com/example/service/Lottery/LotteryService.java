package com.example.service.Lottery;

import com.example.DrawStrategy.DrawResult;

public interface LotteryService {
    public DrawResult draw(Long activityId, Long userId, String clientRequestId);

    Boolean initPrizePool(Long activityId);
}

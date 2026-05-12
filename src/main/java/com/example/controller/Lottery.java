package com.example.controller;


import com.example.service.Lottery.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lottery")
public class Lottery {
    @Autowired
    private LotteryService lotteryService;
    @RequestMapping("/draw")
    public String draw(Long userId, Long activityId, String clientRequestId) {
        return lotteryService.draw(userId, activityId, clientRequestId).toString();
    }
    @RequestMapping("/init")
    public String initPrizePool(Long activityId) {
        return lotteryService.initPrizePool(activityId).toString();
    }
}

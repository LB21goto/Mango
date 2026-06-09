package com.example.controller;

import com.example.annotion.RepeatExecuteLimit;
import com.example.dto.RepeatTestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/test/repeat")
public class RepeatTestController {

    private final AtomicInteger executeCount = new AtomicInteger(0);

    @PostMapping("/submit")
    @RepeatExecuteLimit(
            name = "ORDER_SUBMIT",
            keys = {"#request.userId", "#request.orderId"},
            durationTime = 10,
            message = "订单提交过于频繁，请稍后再试"
    )
    public Map<String, Object> submitOrder(@RequestBody RepeatTestRequest request) {
        int count = executeCount.incrementAndGet();
        log.info("第{}次执行订单提交 - 用户:{}, 订单:{}, 金额:{}",
                count, request.getUserId(), request.getOrderId(), request.getAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "订单提交成功");
        result.put("executeCount", count);
        result.put("transactionId", UUID.randomUUID().toString());
        return result;
    }

    @GetMapping("/payment/{userId}/{orderId}")
    @RepeatExecuteLimit(
            name = "PAYMENT",
            keys = {"#userId", "#orderId"},
            durationTime = 5,
            message = "支付操作过于频繁，请稍后再试"
    )
    public Map<String, Object> processPayment(@PathVariable String userId,
                                              @PathVariable String orderId) {
        int count = executeCount.incrementAndGet();
        log.info("第{}次执行支付 - 用户:{}, 订单:{}", count, userId, orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "支付成功");
        result.put("executeCount", count);
        result.put("paymentNo", UUID.randomUUID().toString());
        return result;
    }

    @PostMapping("/register")
    @RepeatExecuteLimit(
            name = "USER_REGISTER",
            keys = {"#request.userId"},
            durationTime = 30,
            message = "注册请求过于频繁，请稍后再试"
    )
    public Map<String, Object> registerUser(@RequestBody RepeatTestRequest request) {
        int count = executeCount.incrementAndGet();
        log.info("第{}次执行用户注册 - 用户:{}, 描述:{}",
                count, request.getUserId(), request.getDescription());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "注册成功");
        result.put("executeCount", count);
        result.put("userId", request.getUserId());
        return result;
    }

    @GetMapping("/reset")
    public Map<String, Object> resetCounter() {
        executeCount.set(0);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "计数器已重置");
        return result;
    }
}

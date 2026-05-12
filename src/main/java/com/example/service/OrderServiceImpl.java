package com.example.service;

import com.example.entity.Order;
import com.example.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Order getOrder(String orderNo) {
        return null;
    }

    @Override
    public void cancelOrder(String orderNo) {
        // 1. 获取订单信息
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.info("订单不存在：{}", orderNo);
            return;
        }
        // 2. 获取锁
        String lockKey = "order:cancel:lock:" + orderNo;
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1");
        if (!lock) {
            log.info("订单取消失败：{}", orderNo);
        }

        orderMapper.cancelOrder(orderNo,0);
    }
}



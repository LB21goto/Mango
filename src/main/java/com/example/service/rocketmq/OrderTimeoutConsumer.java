package com.example.service.rocketmq;

import com.example.entity.Order;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
@Slf4j
    @RocketMQMessageListener(consumerGroup = "damai-order_timeout_group",topic = "order_timeout_topic")
    public class OrderTimeoutConsumer implements RocketMQListener<String> {

        @Autowired
        private OrderService orderService;

        @Autowired
        private RedisTemplate<String, Object> redisTemplate;

        @Override
        public void onMessage(String orderNo) {

            orderService.cancelOrder(orderNo);
            log.info("订单 {} 超时，座位回滚成功", orderNo);


    }
}

package com.example.service.rocketmq;

import com.example.entity.Order;
import com.example.entity.OrderMessage;
import com.example.mapper.OrderMapper;
import com.example.mapper.SeatMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
// consumerGroup 是消费者组名，必须唯一
// topic 要和 Producer 里定义的一样
@RocketMQMessageListener(consumerGroup = "order-consumer-group", topic = "damai-create_order")
public class OrderConsumer implements RocketMQListener<OrderMessage> {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SeatMapper seatMapper;

    @Override
    public void onMessage(OrderMessage message) {
        try {
            // 1. 幂等性检查：防止重复消费
            // 假设你的 Mapper 里有一个根据 orderNo 查询的方法
            Order existOrder = orderMapper.selectByOrderNo(message.getOrderNo());
            if (existOrder != null) {
                System.out.println("订单已存在，跳过：" + message.getOrderNo());
                return; // 已经处理过了，直接返回
            }
            seatMapper.lockSeat(message.getSeatId());

            // 2. 组装订单对象
            Order order = new Order();
            order.setOrderNo(message.getOrderNo());
            order.setUserId(message.getUserId());
            order.setSeatId(message.getSeatId());
            order.setProgramId(message.getProgramId());
            order.setPrice(message.getPrice());
            order.setStatus(1); // 1: 已支付
            order.setCreateTime(LocalDateTime.now());

            // 3. 写入数据库
            orderMapper.insertOrder(order);

            System.out.println("订单入库成功：" + order.getOrderNo());

        } catch (Exception e) {
            e.printStackTrace();
            // 这里抛出异常，RocketMQ 会自动重试
            throw new RuntimeException("订单处理失败，触发重试");
        }
    }


}

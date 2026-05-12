package com.example.service.rocketmq;

import com.example.core.SpringUtil;
import com.example.dto.StockChangeLogDTO;
import com.example.mq.callback.FailureCallback;
import com.example.mq.callback.SuccessCallback;
import com.example.entity.OrderMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @program: 极度真实还原大麦网高并发实战项目。 添加 阿星不是程序员 微信，添加时备注 大麦 来获取项目的完整资料 
 * @description: rocketmq 创建订单 发送
 * @author: 阿星不是程序员
 **/
@Slf4j
@AllArgsConstructor
@Component
public class Orderproducer {
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired
    private RocketMQTopic rocketMQTopic;
    
    public void sendMessage(OrderMessage orderMessage, SuccessCallback<SendResult> successCallback,
                            FailureCallback failureCallback) {
        log.info("创建订单 rocketmq 发送消息 消息体 : {}", orderMessage);
        String destination = SpringUtil.getPrefixDistinctionName() + "-" + rocketMQTopic.getTopic();
        rocketMQTemplate.asyncSend(destination, orderMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                successCallback.onSuccess(sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                failureCallback.onFailure(throwable);
            }

        });
    }
    public void sendDelayMessage(OrderMessage orderMessage, long delayTime, SuccessCallback<SendResult> successCallback){
        //延迟队列消息
        Message delayMsg = new Message("order_timeout_topic",
                orderMessage.getOrderNo().getBytes(StandardCharsets.UTF_8));
        // RocketMQ 延迟等级：3
        delayMsg.setDelayTimeLevel(3);
        try {
            rocketMQTemplate.getProducer().send(delayMsg);
            log.info("订单 {} 延迟消息发送成功", orderMessage.getOrderNo());
        } catch (Exception e) {
            log.error("订单 {} 延迟消息发送失败", orderMessage.getOrderNo(), e);
        }
    }

    public void sendStockLog(StockChangeLogDTO logDTO, SuccessCallback<SendResult> successCallback,
                             FailureCallback failureCallback) {
        log.info("库存流水 MQ 发送消息 消息体：{}", logDTO);
        String destination = "damai-stock_change_log_topic";
        rocketMQTemplate.asyncSend(destination, logDTO, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                successCallback.onSuccess(sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                failureCallback.onFailure(throwable);
            }
        });
    }

    public void sendStockLog(StockChangeLogDTO logDTO) {
        sendStockLog(logDTO,
                sendResult -> log.info("库存流水 MQ 发送成功：{}", sendResult.getMsgId()),
                throwable -> log.error("库存流水 MQ 发送失败", throwable));
    }


}

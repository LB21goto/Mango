package com.example.entity;



import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单消息体
 * 必须实现 Serializable，否则 RocketMQ 序列化会报错
 */
@Data
public class OrderMessage implements Serializable {
    private Long userId;
    private Long seatId;
    private Long programId;
    private BigDecimal price;
    private String orderNo; // 订单号作为唯一标识，用于幂等性校验
    private Long expireTime = 1800L;
}
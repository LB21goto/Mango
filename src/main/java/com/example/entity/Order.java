package com.example.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class Order {
    // 主键
    private Long id;

    // 业务字段

    private String orderNo;     // 订单号 (业务唯一标识)
    private Long userId;        // 用户ID
    private Long seatId;     // 节目/演出ID (对应你表里的 progaram_id)
    private BigDecimal price;   // 价格
    private Integer status;     // 状态：0未支付，1已支付等
    private Long programId;
    // 时间字段
    private LocalDateTime createTime;
    private LocalDateTime updateTime;













}
package com.example.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变更流水 DTO
 * 用于 MQ 消息传输
 */
@Data
public class StockChangeLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水唯一标识（雪花算法生成）
     */
    private String logId;

    /**
     * 操作类型：LOCK_SEAT/RELEASE_SEAT/CREATE_ORDER/CANCEL_ORDER
     */
    private String operationType;

    /**
     * 节目 ID 或座位 ID
     */
    private Long ticketCategoryId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 变更数量（-1 扣减，+1 回滚，0 确认）
     */
    private Integer changeAmount;

    /**
     * 变更前库存快照（暂时用 0 占位）
     */
    private Integer beforeStock;

    /**
     * 变更后库存快照（暂时用 0 占位）
     */
    private Integer afterStock;

    /**
     * 操作唯一标识（防重用）
     * 格式：seatId:LOCK:timestamp:userId 或 orderNo:CANCEL:timestamp
     */
    private String identifierId;

    /**
     * Redis 操作时间戳（毫秒）
     */
    private Long redisOpTime;

    /**
     * 来源：1-用户下单 2-超时取消 3-后台调整
     */
    private Integer source;

    /**
     * 状态：1-已扣减 2-已落库 3-对账异常
     */
    private Integer status;

    /**
     * 对账失败原因或异常信息
     */
    private String errorMessage;
    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime = java.time.LocalDateTime.now();
}

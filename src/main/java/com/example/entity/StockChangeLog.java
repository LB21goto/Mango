package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存变更流水实体
 * 对应数据库表：stock_change_log
 */
@Data
public class StockChangeLog {

    /**
     * 主键 ID（自增）
     */
    private Long id;

    /**
     * 流水唯一标识（雪花算法）
     */
    private String logId;

    /**
     * 操作类型
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
     * 变更数量
     */
    private Integer changeAmount;

    /**
     * 变更前库存快照
     */
    private Integer beforeStock;

    /**
     * 变更后库存快照
     */
    private Integer afterStock;

    /**
     * 操作唯一标识
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
     * 对账失败原因
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

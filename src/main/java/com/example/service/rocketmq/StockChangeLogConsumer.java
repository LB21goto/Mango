package com.example.service.rocketmq;

import com.example.dto.StockChangeLogDTO;
import com.example.entity.StockChangeLog;
import com.example.mapper.SeatMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "stock-log-consumer-group", topic = "damai-stock_change_log_topic")
public class StockChangeLogConsumer implements RocketMQListener<StockChangeLogDTO> {

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public void onMessage(StockChangeLogDTO dto) {
        try {
            log.info("收到库存流水消息：{}", dto);
            
            StockChangeLog logEntity = new StockChangeLog();
            BeanUtils.copyProperties(dto, logEntity);
            
            seatMapper.insertStockLog(logEntity);
            
            log.info("库存流水落库成功：{}", dto.getLogId());
        } catch (Exception e) {
            log.error("库存流水落库失败：{}", dto, e);
            throw new RuntimeException(e);
        }
    }
}

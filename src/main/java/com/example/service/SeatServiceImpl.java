package com.example.service;

import com.example.dto.StockChangeLogDTO;
import com.example.mq.callback.FailureCallback;
import com.example.mq.callback.SuccessCallback;
import java.util.Random;
import com.example.entity.OrderMessage;
import com.example.mapper.OrderMapper;
import com.example.mapper.SeatMapper;
import com.example.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import com.example.service.rocketmq.Orderproducer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Service
public class SeatServiceImpl implements SeatService{

    @Autowired
    private SeatMapper seatMapper;
    @Autowired
    private OrderMapper orderMapper;
    // 1. 新注入：刚刚写的 MQ 生产者
    @Autowired
    private Orderproducer orderproducer;
    private Random random = new Random();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DefaultRedisScript<Long> seatLockScript;



    @Override
    public String getseat(Long userId, Long seatId) {
        // ✅ 2. 生成订单号
        String orderNo = generateOrderNo(userId);


        String bitmapKey = "seat:bitmap:1";  // 节目 ID=1
        String hashKey = "seat:hash:1";
        String stockKey = "stock:1001";
        String logQueueKey = "stock:log:queue";  // 或其他业务定义的 key
        long timestamp = System.currentTimeMillis();

        // ✅ 1. Redis Lua 锁座（替代 DB）
        log.info("Lua 参数：seatId={}, userId={}, orderNo={}, timestamp={}", seatId, userId, orderNo, timestamp);

        Long result = stringRedisTemplate.execute(
                seatLockScript,
                Arrays.asList(bitmapKey, hashKey, stockKey,logQueueKey),
                seatId.toString(),
                userId.toString(),
                orderNo,
                String.valueOf(timestamp),
                "LOCK"
        );
        if (result == null || result == 0) {
            throw new RuntimeException("抢座失败，手慢了！");
        }

        // ==================== 【新增】在这里写入流水 ====================
// 触发时机：Redis Lua 返回 result=1，表示锁座成功
        StockChangeLogDTO logDTO = new StockChangeLogDTO();
        logDTO.setLogId(generateOrderNo(userId));  // 生成雪花 ID
        logDTO.setOperationType("LOCK_SEAT");
        logDTO.setTicketCategoryId(1L);  // 节目 ID
        logDTO.setOrderNo(orderNo);
        logDTO.setUserId(userId);
        logDTO.setChangeAmount(-1);  // 扣减 1 个座位
        logDTO.setBeforeStock(0);    // 暂时用 0 占位
        logDTO.setAfterStock(0);     // 暂时用 0 占位
        logDTO.setIdentifierId(seatId + ":LOCK:" + timestamp + ":" + userId);
        logDTO.setRedisOpTime(timestamp);
        logDTO.setSource(1);  // 用户下单
        logDTO.setStatus(1);  // 已扣减
        logDTO.setCreateTime(LocalDateTime.now());

        // 发送到 MQ（异步落库）
        orderproducer.sendStockLog(logDTO);

        // ⚠️ 这里建议你提前缓存 price（否则又打 DB）
        BigDecimal price = new BigDecimal(random.nextInt(100,999));

        // ✅ 3. 构建 MQ 消息
        OrderMessage message = new OrderMessage();
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setProgramId(1L);
        message.setSeatId(seatId);
        message.setPrice(price);

        // ✅ 4. 发 MQ（削峰）
        orderproducer.sendMessage(message,
                sendResult -> log.info("MQ 发送成功：{}", sendResult.getMsgId()),
                throwable -> log.error("MQ 发送失败", throwable));
        //4.1延迟队列
        orderproducer.sendDelayMessage(message, 5000, sendResult ->
                log.info("MQ 延迟发送成功：{}", sendResult.getMsgId()));

        // ✅ 5. 返回
        return "抢票成功！订单号：" + orderNo;
    }

    /**
     * 生成唯一订单号工具
     * 规则：yyyyMMddHHmmss + userId后4位 + 4位随机数
     */
    private String orderNoPrefix = "ORD";

    private String generateOrderNo(Long userId) {
        StringBuilder sb = new StringBuilder();
        // 时间戳部分 (精确到秒)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        sb.append(timestamp);

        // 用户标识部分 (取模补齐，防冲突)
        String userSuffix = String.format("%04d", userId % 10000);
        sb.append(userSuffix);

        // 随机数部分 (防并发重复)
        int random = (int) (Math.random() * 9000) + 1000;
        sb.append(random);

        return sb.toString();


    }



    @Override
    public String releseseat(Long seatid) {
        int update = seatMapper.releseSeat(seatid);
        if (update == 0) {
            throw new RuntimeException("抢座失败，手慢了！");
        }
        return "成功";
    }
}

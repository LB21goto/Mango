package com.example.DrawStrategy;

import com.example.entity.Prize;
import com.example.entity.Activity;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("virtualImmediateDispatcher")
public class VirtualImmediateDispatcher implements AwardDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(VirtualImmediateDispatcher.class);

    // TODO: 注入你项目中用于写入发奖记录/券码表的 mapper/repo
    // @Autowired private UserDrawRecordMapper userDrawRecordMapper;
    // @Autowired private CouponService couponService;

    @Override
    public String getName() { return "virtualImmediateDispatcher"; }

    @Override
    public DeliverResult deliver(Long userId, Prize prize, Activity activity, String requestId) {
        // 1. 幂等检查（示例：在 DB 中基于 requestId 做唯一索引）
        // if (userDrawRecordMapper.existsByRequestId(requestId)) {
        //    return new DeliverResult(DeliverResult.Status.DELIVERED, "重复请求，幂等已处理");
        // }

        try {
            // 2. 生成券码或直接调用券服务
            // String code = couponService.generateAndBind(userId, prize.getCouponTemplateId(), requestId);

            // 3. 持久化：写 user_draw_record (status=DELIVERED), 写入券码等
            // userDrawRecordMapper.insert(...)

            // 示例返回（请替换为真实返回值）
            String deliveryId = "coupon-" + requestId;
            logger.info("虚拟奖品即时发放 userId={} prizeId={} requestId={} deliveryId={}", userId, prize.getId(), requestId, deliveryId);
            return new DeliverResult(DeliverResult.Status.DELIVERED, "虚拟奖品已发放", deliveryId);
        } catch (Exception ex) {
            logger.error("虚拟奖品发放失败 userId={} prizeId={} requestId={}", userId, prize.getId(), requestId, ex);
            return new DeliverResult(DeliverResult.Status.FAILED, "虚拟奖品发放失败");
        }
    }
}

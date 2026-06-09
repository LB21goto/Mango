package com.example.service;

import com.example.dto.BitmapResponse;
import com.google.common.collect.Range;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SeatBitmapServiceImpl implements SeatBitmapService{

    private final StringRedisTemplate redisTemplate;

    public SeatBitmapServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在 Redis 中对 keyA 和 keyB 做 BITOP OR 到临时 key，读取前 totalBits 位并返回 0/1 列表，
     * 最后删除临时 key（可选）。
     */
    public BitmapResponse orAndReadBits(String keyA, String keyB, int totalBits) {
        // 生成一个临时 key，避免并发冲突可以加入 UUID
        String destKey = "tmp:bitmap:or:" + UUID.randomUUID();

        // 执行 BITOP OR destKey keyA keyB
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            byte[] destBytes = destKey.getBytes(StandardCharsets.UTF_8);
            byte[] aBytes = keyA.getBytes(StandardCharsets.UTF_8);
            byte[] bBytes = keyB.getBytes(StandardCharsets.UTF_8);

            // 使用底层 connection 的 bitOp
            connection.bitOp(RedisStringCommands.BitOperation.OR, destBytes, aBytes, bBytes);
            return null;
        });

        // 读取每一位（使用 opsForValue().getBit 确保位语义与 Redis GETBIT 保持一致）
        List<Integer> bits = new ArrayList<>(totalBits);
        for (int i = 0; i < totalBits; i++) {
            Boolean bit = redisTemplate.opsForValue().getBit(destKey, i);
            bits.add(Boolean.TRUE.equals(bit) ? 1 : 0);
        }

        // 删除临时 key（如果你想保留也可以删除此行）
        redisTemplate.delete(destKey);

        BitmapResponse resp = new BitmapResponse();
        resp.setBits(bits);
        return resp;
    }
    /**
     * 获取已售座位列表
     * @param eventId 演出/场次 ID，用于构造 bitmap key，例如 "seat:bitmap:{eventId}"
     * @param zeroBased 是否返回 0-based 索引（true 返回 0-based，false 返回 1-based，默认 false）
     * @return 已售座位索引列表（升序）
     */
    @Override
    public List<Integer> getSoldSeats(String eventId, boolean zeroBased) {
        String key = "seat:bitmap:" + eventId;
        List<Integer> seats = new ArrayList<>();

        try (RedisConnection conn = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()) {
            // 1. 一次性获取整个 Bitmap 的字节数组 (1次网络请求)
            byte[] bitmapBytes = conn.stringCommands().get(key.getBytes(StandardCharsets.UTF_8));

            if (bitmapBytes == null || bitmapBytes.length == 0) {
                return seats; // Key不存在或为空
            }

            // 2. 在本地内存中解析字节数组，提取为1的位
            for (int byteIndex = 0; byteIndex < bitmapBytes.length; byteIndex++) {
                byte b = bitmapBytes[byteIndex];

                // 如果这个字节全是0，直接跳过，提升速度
                if (b == 0) continue;

                // 遍历字节中的每一位 (从高位到低位: 7, 6, 5, ..., 0)
                for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                    // 判断该位是否为 1
                    if (((b >> bitIndex) & 1) == 1) {
                        // 计算全局偏移量 (座位号)
                        long globalPos = (long) byteIndex * 8 + (7 - bitIndex);
                        int seatNumber = zeroBased ? (int) globalPos : (int) globalPos + 1;
                        seats.add(seatNumber);
                    }
                }
            }
        }

        return seats;
    }
}
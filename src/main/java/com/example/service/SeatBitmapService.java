package com.example.service;

import com.example.dto.BitmapResponse;
import java.util.List;

/**
 * Seat Bitmap Service 接口
 */

public interface SeatBitmapService {

    /**
     * 获取已售座位列表
     * @param eventId 演出/场次 ID
     * @param zeroBased 是否返回 0-based 索引
     * @return 已售座位索引列表（升序）
     */
    List<Integer> getSoldSeats(String eventId, boolean zeroBased);

    /**
     * 将两个 Redis bitmap 做 OR 操作，并返回前 totalBits 位的 0/1 数组
     * @param keyA 第一个 bitmap 键
     * @param keyB 第二个 bitmap 键
     * @param totalBits 要读取的位数
     * @return 包含 0/1 数组的响应对象
     */
    BitmapResponse orAndReadBits(String keyA, String keyB, int totalBits);
}

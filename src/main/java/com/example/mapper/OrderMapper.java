package com.example.mapper;


import com.example.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface OrderMapper {

    /**
     * 插入订单
     * useGeneratedKeys=true, keyProperty="id" 用于获取自增主键
     */
    @Insert("INSERT INTO test_order (order_no, user_id, program_id, seat_id , price, status, create_time) " +
            "VALUES (#{orderNo}, #{userId}, #{programId}, #{seatId}, #{price}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(Order order);

    @Select("SELECT * FROM test_order WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    @Update("UPDATE test_order SET status = #{status} WHERE order_no = #{orderNo}")
    int cancelOrder(@Param("orderNo") String orderNo, @Param("status") Integer status);

    @Select("SELECT * FROM test_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(Long userId);

}

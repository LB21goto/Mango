package com.example.mapper;


import com.example.entity.StockChangeLog;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface SeatMapper {

    @Update("UPDATE test_seat SET status = 2 WHERE id = #{seatId} AND status = 1")
    int lockSeat(@Param("seatId") Long seatId);
    @Update("UPDATE test_seat SET status = 1 WHERE id = #{seatId} AND status = 2")
    int releseSeat(@Param("seatId") Long seatId);


    @Select("select price from test_seat where id = #{seatId}")
    BigDecimal getPrice(@Param("seatId") Long seatId);

    void insertStockLog(StockChangeLog logEntity);
}

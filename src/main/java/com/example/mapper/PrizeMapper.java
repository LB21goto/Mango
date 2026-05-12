package com.example.mapper;

import com.example.entity.Prize;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PrizeMapper {

    @Select("SELECT * FROM prize WHERE id = #{prizeId}")
    Prize findById(Long prizeId);

    @Select("SELECT * FROM prize WHERE activity_id = #{activityId}")
    List<Prize> findByActivityId(Long activityId);
}

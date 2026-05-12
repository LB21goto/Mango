package com.example.mapper;

import com.example.entity.Activity;
import org.apache.ibatis.annotations.Select;

public interface ActivityMapper {

    @Select("SELECT * FROM activity WHERE id = #{activityId}")
    Activity findById(Long activityId);
}

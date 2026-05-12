package com.Ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface Aimapper {
    @Select("SELECT magazine FROM weapons WHERE weapons_name = #{name}")
    int findMagzineByName(String name);

    @Update("UPDATE weapons SET magazine = #{num} WHERE weapons_name = #{name}")
    int changeMagzineByName(String name,int num);
}

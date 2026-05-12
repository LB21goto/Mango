package com.example.mapper;

import com.example.entity.Program;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProgramMapper {
    Program selectById(@Param("id") Long id);
}

package com.Ai;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public interface Aiservice {
    int getmag(@ToolParam(description = "查询的条件", required = true) String condition);

    int changemag(@ToolParam(description = "查询的条件", required = true) String name,@ToolParam(description = "设置的弹容量", required = true) int num);
}

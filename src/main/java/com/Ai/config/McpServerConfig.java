package com.Ai.config;

import com.Ai.Mcptools;
import org.checkerframework.checker.units.qual.A;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class McpServerConfig {

    @Autowired
    private Mcptools mcptools;
    /**
     * 注册考勤工具
     */
    @Bean
    public ToolCallbackProvider mcpToolCallbackProvider() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcptools)
                .build();
    }

}

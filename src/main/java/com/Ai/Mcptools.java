package com.Ai;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Component
public class Mcptools {
    @Tool(description = "将内容写入文件，如果文件存在则覆盖")
    public String WriteFile(
            @ToolParam(description = "文件路径") String filePath,
            @ToolParam(description = "要写入的内容") String content
    ) {
        try {
            Path path = Paths.get(filePath);
            // 确保父目录存在
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            // 写入文件（覆盖模式）
            Files.writeString(path, content);
            return "写入成功：" + path.toAbsolutePath();
        } catch (IOException e) {
            return "写入失败：" + e.getMessage();
        }
    }
    @Autowired
    private AiserviceImpl aiserviceimpl;


    @Tool(description = "当用户询问武器的弹匣容量、载弹量时使用。通过传入武器名称查询本地数据库获取准确的弹容量")
    public Integer getmag(@ToolParam(description = "具体的武器名称，例如：AK47、M4", required = false) String condition) {

        try {
            Integer result = aiserviceimpl.getmag(condition);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
    @Tool(description = "当用户需要修改武器的弹匣容量、载弹量时使用。通过传入武器名称,弹容量修改本地数据的弹容量")
    public Integer changemag(@ToolParam(description = "具体的武器名称，例如：AK47、M4", required = false) String name ,@ToolParam(description = "具体的武器弹容量，例如：200、M30", required = false)int num) {

        try {
            Integer result = aiserviceimpl.changemag(name,num);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
    @Tool(description = "当用户询问时间时使用。通过传入时区查询当前时间")
    public String gettime(@ToolParam(description = "具体的时区，例如：UTC、GMT+8", required = false) String condition) {
        try {
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of(condition));
            return now.toString();
        } catch (Exception e) {
            throw e;
        }
    }

}

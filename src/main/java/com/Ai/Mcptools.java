package com.Ai;

import com.Ai.Service.AiserviceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    @Tool(description = "检测用户消息中的购票意图，返回结构化的票务信息给前端回显")
    public Map<String, Object> detectTicketIntent(
            @ToolParam(description = "用户输入的消息内容") String message,
            @ToolParam(description = "会话ID，用于追踪上下文", required = false) String chatId
    ) {
        Map<String, Object> result = new HashMap<>();

        // 购票关键词匹配
        boolean hasIntent = message.contains("买票") || message.contains("订票") ||
                message.contains("购票") || message.contains("抢票") ||
                message.contains("演唱会") || message.contains("演出");

        if (!hasIntent) {
            result.put("hasTicketIntent", false);
            result.put("message", message);
            return result;
        }

        // 构建要传给前端的票务数据
        Map<String, Object> ticketData = new HashMap<>();

        // 提取演出名称
        String eventName = extractEventName(message);
        ticketData.put("eventName", eventName != null ? eventName : "待确认");

        // 提取演出时间
        String showTime = extractShowTime(message);
        ticketData.put("showTime", showTime != null ? showTime : "待确认");

        // 提取场馆
        String venue = extractVenue(message);
        ticketData.put("venue", venue != null ? venue : "待确认");

        // 座位信息（示例数据，实际可根据规则或调用接口获取）
        List<Map<String, String>> seats = new ArrayList<>();
        Map<String, String> seat = new HashMap<>();
        seat.put("area", extractArea(message));
        seat.put("row", extractRow(message));
        seat.put("seat", extractSeatNumber(message));
        seats.add(seat);
        ticketData.put("seats", seats);

        // 价格
        Integer price = extractPrice(message);
        ticketData.put("price", price != null ? price : 0);

        // 购票人信息（示例）
        Map<String, String> buyerInfo = new HashMap<>();
        buyerInfo.put("name", extractBuyerName(message));
        buyerInfo.put("phone", extractPhone(message));
        ticketData.put("buyerInfo", buyerInfo);

        // 将数据转换为JSON字符串
        String ticketIntentJson = toTicketIntentFormat(ticketData);

        // 返回结果，前端会解析 __TICKET_INTENT__ 格式
        result.put("hasTicketIntent", true);
        result.put("ticketIntent", ticketIntentJson);
        result.put("displayMessage", generateDisplayMessage(ticketData));

        return result;
    }

    // 转换为前端需要的格式
    private String toTicketIntentFormat(Map<String, Object> ticketData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(ticketData);
            return "__TICKET_INTENT__\n" + json + "\n__TICKET_INTENT_END__";
        } catch (Exception e) {
            return "";
        }
    }

    // 生成前端回显的提示消息
    private String generateDisplayMessage(Map<String, Object> ticketData) {
        return String.format("好的，我来帮您下单...\n\n【演出】%s\n【时间】%s\n【场馆】%s\n【票价】%d元\n【座位】%s区%s排%s号\n【购票人】%s %s\n\n请确认信息是否正确？",
                ticketData.get("eventName"),
                ticketData.get("showTime"),
                ticketData.get("venue"),
                ticketData.get("price"),
                ((List<Map<String, String>>) ticketData.get("seats")).get(0).get("area"),
                ((List<Map<String, String>>) ticketData.get("seats")).get(0).get("row"),
                ((List<Map<String, String>>) ticketData.get("seats")).get(0).get("seat"),
                ((Map<String, String>) ticketData.get("buyerInfo")).get("name"),
                ((Map<String, String>) ticketData.get("buyerInfo")).get("phone")
        );
    }

    // 辅助提取方法（简化版）
    private String extractEventName(String message) {
        Pattern p = Pattern.compile("(?:买|订|抢)(.+?)(?:演唱会|演出|的票)");
        Matcher m = p.matcher(message);
        if (m.find()) return m.group(1);
        if (message.contains("周杰伦")) return "周杰伦演唱会";
        if (message.contains("五月天")) return "五月天演唱会";
        return null;
    }

    private String extractShowTime(String message) {
        if (message.contains("周六")) {
            return java.time.LocalDate.now().plusDays(6 - java.time.LocalDate.now().getDayOfWeek().getValue()) + " 20:00";
        }
        if (message.contains("周日")) {
            return java.time.LocalDate.now().plusDays(7 - java.time.LocalDate.now().getDayOfWeek().getValue()) + " 20:00";
        }
        return null;
    }

    private String extractVenue(String message) {
        Pattern p = Pattern.compile("在(北京|上海|广州|深圳|杭州|成都|武汉|西安|重庆|天津)");
        Matcher m = p.matcher(message);
        if (m.find()) return m.group(1) + "体育场";
        return null;
    }

    private String extractArea(String message) {
        return "A";
    }

    private String extractRow(String message) {
        return "3";
    }

    private String extractSeatNumber(String message) {
        return "15";
    }

    private Integer extractPrice(String message) {
        Pattern p = Pattern.compile("(\\d+)(?:元|块)");
        Matcher m = p.matcher(message);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 1280;
    }

    private String extractBuyerName(String message) {
        return "张三";
    }

    private String extractPhone(String message) {
        return "138****1234";
    }

}

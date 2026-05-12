//package com.Ai;
//
//import org.springframework.ai.tool.annotation.Tool;
//import org.springframework.ai.tool.annotation.ToolParam;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class Aitools {
//    @Autowired
//    private AiserviceImpl aiserviceimpl;
//
//
//    @Tool(description = "当用户询问武器的弹匣容量、载弹量时使用。通过传入武器名称查询本地数据库获取准确的弹容量")
//    public Integer getmag(@ToolParam(description = "具体的武器名称，例如：AK47、M4", required = false) String condition) {
//
//        try {
//            Integer result = aiserviceimpl.getmag(condition);
//            return result;
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//    @Tool(description = "当用户询问时间时使用。通过传入时区查询当前时间")
//    public String gettime(@ToolParam(description = "具体的时区，例如：UTC、GMT+8", required = false) String condition) {
//        try {
//            java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of(condition));
//            return now.toString();
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//}

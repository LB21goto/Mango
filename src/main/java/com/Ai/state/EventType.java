package com.Ai.state;

public enum EventType {
    START("启动"),
    INTENT_RECOGNIZED("意图识别"),
    PROCESSING("处理"),
    WAITING_CONFIRMATION("等待确认"),
    COMPLETE("完成"),
    ERROR("错误"),
    RESET("重置");

    private final String desc;

    EventType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
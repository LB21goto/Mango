package com.Ai.state;

public enum ChatState {
    START("开始"),
    INTENT_RECOGNIZED("意图已识别"),
    PROCESSING("处理中"),
    WAITING_CONFIRMATION("等待确认"),
    COMPLETED("已完成"),
    ERROR("错误");

    private final String desc;

    ChatState(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
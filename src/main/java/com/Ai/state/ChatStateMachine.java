package com.Ai.state;

import com.Ai.RouteType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ChatStateMachine {

    private ChatState currentState = ChatState.START;

    private final Map<String, ChatState> transitionRules = new HashMap<>();

    public ChatStateMachine() {
        initTransitionRules();
    }

    private void initTransitionRules() {
        // 定义所有允许的状态转换规则
        // 格式: "当前状态_事件" -> "下一个状态"

        // START 状态的转换
        addRule(ChatState.START, EventType.START, ChatState.START);
        addRule(ChatState.START, EventType.INTENT_RECOGNIZED, ChatState.INTENT_RECOGNIZED);

        // INTENT_RECOGNIZED 状态的转换
        addRule(ChatState.INTENT_RECOGNIZED, EventType.PROCESSING, ChatState.PROCESSING);
        addRule(ChatState.INTENT_RECOGNIZED, EventType.ERROR, ChatState.ERROR);

        // PROCESSING 状态的转换
        addRule(ChatState.PROCESSING, EventType.WAITING_CONFIRMATION, ChatState.WAITING_CONFIRMATION);
        addRule(ChatState.PROCESSING, EventType.COMPLETE, ChatState.COMPLETED);
        addRule(ChatState.PROCESSING, EventType.ERROR, ChatState.ERROR);

        // WAITING_CONFIRMATION 状态的转换
        addRule(ChatState.WAITING_CONFIRMATION, EventType.PROCESSING, ChatState.PROCESSING);
        addRule(ChatState.WAITING_CONFIRMATION, EventType.COMPLETE, ChatState.COMPLETED);
        addRule(ChatState.WAITING_CONFIRMATION, EventType.ERROR, ChatState.ERROR);

        // COMPLETED 状态的转换（只能重置）
        addRule(ChatState.COMPLETED, EventType.RESET, ChatState.START);

        // ERROR 状态的转换（只能重置）
        addRule(ChatState.ERROR, EventType.RESET, ChatState.START);
    }

    private void addRule(ChatState from, EventType event, ChatState to) {
        String key = from.name() + "_" + event.name();
        transitionRules.put(key, to);
        log.info("注册状态转换规则: {} --({})--> {}", from.getDesc(), event.getDesc(), to.getDesc());
    }

    public boolean canTransition(EventType event) {
        String key = currentState.name() + "_" + event.name();
        return transitionRules.containsKey(key);
    }

    public void trigger(EventType event) {
        String key = currentState.name() + "_" + event.name();
        ChatState nextState = transitionRules.get(key);

        if (nextState == null) {
            log.warn("【状态机】非法转换: {} --({})--> ? 当前状态不允许此事件",
                    currentState.getDesc(), event.getDesc());
            throw new IllegalStateException(
                    String.format("状态 %s 不允许事件 %s", currentState.getDesc(), event.getDesc())
            );
        }

        log.info("【状态机】{} --({})--> {}", currentState.getDesc(), event.getDesc(), nextState.getDesc());
        currentState = nextState;
    }

    public void start() {
        trigger(EventType.START);
    }

    public void onIntentRecognized(RouteType routeType) {
        log.info("识别到意图: {}", routeType);
        trigger(EventType.INTENT_RECOGNIZED);
    }

    public void onProcessing() {
        trigger(EventType.PROCESSING);
    }

    public void onWaitingConfirmation() {
        trigger(EventType.WAITING_CONFIRMATION);
    }

    public void onComplete() {
        trigger(EventType.COMPLETE);
    }

    public void onError(Exception e) {
        log.error("发生错误: {}", e.getMessage());
        trigger(EventType.ERROR);
    }

    public void reset() {
        log.info("状态机重置");
        currentState = ChatState.START;
    }

    public ChatState getCurrentState() {
        return currentState;
    }

    public Map<String, ChatState> getAllRules() {
        return transitionRules;
    }
}
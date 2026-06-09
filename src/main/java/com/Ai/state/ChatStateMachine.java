package com.Ai.state;

import com.Ai.RouteType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatStateMachine {

    private ChatState currentState = ChatState.START;

    public void start() {
        transition(ChatState.START);
    }

    public void onIntentRecognized(RouteType routeType) {
        log.info("状态转换: {} -> INTENT_RECOGNIZED, 意图: {}", currentState, routeType);
        transition(ChatState.INTENT_RECOGNIZED);
    }

    public void onProcessing() {
        log.info("状态转换: {} -> PROCESSING", currentState);
        transition(ChatState.PROCESSING);
    }

    public void onWaitingConfirmation() {
        log.info("状态转换: {} -> WAITING_CONFIRMATION", currentState);
        transition(ChatState.WAITING_CONFIRMATION);
    }

    public void onComplete() {
        log.info("状态转换: {} -> COMPLETED", currentState);
        transition(ChatState.COMPLETED);
    }

    public void onError(Exception e) {
        log.error("状态转换: {} -> ERROR, 错误: {}", currentState, e.getMessage());
        transition(ChatState.ERROR);
    }

    public void reset() {
        log.info("状态机重置");
        currentState = ChatState.START;
    }

    private void transition(ChatState newState) {
        log.info("【状态机】{} -> {}", currentState.getDesc(), newState.getDesc());
        currentState = newState;
    }

    public ChatState getCurrentState() {
        return currentState;
    }
}
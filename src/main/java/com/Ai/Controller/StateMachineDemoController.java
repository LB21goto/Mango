package com.Ai.Controller;

import com.Ai.RouteType;
import com.Ai.state.ChatState;
import com.Ai.state.ChatStateMachine;
import com.Ai.state.EventType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/state-machine")
public class StateMachineDemoController {

    private final ChatStateMachine stateMachine;

    public StateMachineDemoController(ChatStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostMapping("/trigger")
    public Map<String, Object> trigger(@RequestParam String event) {
        try {
            EventType eventType = EventType.valueOf(event);

            if (!stateMachine.canTransition(eventType)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "当前状态不允许此事件");
                error.put("currentState", stateMachine.getCurrentState().name());
                error.put("event", event);
                return error;
            }

            stateMachine.trigger(eventType);
            return response("状态转换成功");
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentState", stateMachine.getCurrentState().name());
        result.put("description", stateMachine.getCurrentState().getDesc());
        return result;
    }

    @GetMapping("/rules")
    public Map<String, Object> getAllRules() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentState", stateMachine.getCurrentState().name());

        List<Map<String, String>> rules = stateMachine.getAllRules().entrySet().stream()
                .map(entry -> {
                    Map<String, String> rule = new HashMap<>();
                    String[] parts = entry.getKey().split("_");
                    rule.put("fromState", parts[0]);
                    rule.put("event", parts[1]);
                    rule.put("toState", entry.getValue().name());
                    return rule;
                })
                .collect(Collectors.toList());

        result.put("transitionRules", rules);
        return result;
    }

    @PostMapping("/demo-ticket-flow")
    public Map<String, Object> demoTicketFlow() {
        stateMachine.reset();

        System.out.println("\n=== 演示：购票流程 ===");
        stateMachine.trigger(EventType.START);
        stateMachine.onIntentRecognized(RouteType.TICKET_PURCHASE);
        stateMachine.onProcessing();
        stateMachine.onWaitingConfirmation();
        stateMachine.onComplete();

        return response("购票流程演示完成");
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        stateMachine.reset();
        return response("状态机已重置");
    }

    private Map<String, Object> response(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("currentState", stateMachine.getCurrentState().name());
        result.put("description", stateMachine.getCurrentState().getDesc());
        return result;
    }
}
package com.Ai.Controller;

import com.Ai.RouteType;
import com.Ai.state.ChatStateMachine;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/state-machine")
public class StateMachineDemoController {

    private final ChatStateMachine stateMachine;

    public StateMachineDemoController(ChatStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostMapping("/start")
    public Map<String, Object> start() {
        stateMachine.start();
        return response("状态机启动");
    }

    @PostMapping("/intent")
    public Map<String, Object> recognizeIntent(@RequestParam String intent) {
        RouteType routeType = RouteType.valueOf(intent);
        stateMachine.onIntentRecognized(routeType);
        return response("意图识别: " + intent);
    }

    @PostMapping("/processing")
    public Map<String, Object> processing() {
        stateMachine.onProcessing();
        return response("开始处理");
    }

    @PostMapping("/confirmation")
    public Map<String, Object> waitingConfirmation() {
        stateMachine.onWaitingConfirmation();
        return response("等待确认");
    }

    @PostMapping("/complete")
    public Map<String, Object> complete() {
        stateMachine.onComplete();
        return response("完成");
    }

    @PostMapping("/error")
    public Map<String, Object> error(@RequestParam(required = false, defaultValue = "测试错误") String message) {
        stateMachine.onError(new Exception(message));
        return response("发生错误: " + message);
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        stateMachine.reset();
        return response("状态机已重置");
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentState", stateMachine.getCurrentState().name());
        result.put("description", stateMachine.getCurrentState().getDesc());
        return result;
    }

    @PostMapping("/demo-ticket-flow")
    public Map<String, Object> demoTicketFlow() {
        stateMachine.reset();

        System.out.println("\n=== 演示：购票流程 ===");
        stateMachine.start();
        stateMachine.onIntentRecognized(RouteType.TICKET_PURCHASE);
        stateMachine.onProcessing();
        stateMachine.onWaitingConfirmation();
        stateMachine.onComplete();

        return response("购票流程演示完成");
    }

    @PostMapping("/demo-chat-flow")
    public Map<String, Object> demoChatFlow() {
        stateMachine.reset();

        System.out.println("\n=== 演示：闲聊流程 ===");
        stateMachine.start();
        stateMachine.onIntentRecognized(RouteType.CHAT);
        stateMachine.onProcessing();
        stateMachine.onComplete();

        return response("闲聊流程演示完成");
    }

    private Map<String, Object> response(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("currentState", stateMachine.getCurrentState().name());
        result.put("description", stateMachine.getCurrentState().getDesc());
        return result;
    }
}
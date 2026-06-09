package com.Ai.Controller;

import com.Ai.Service.OpenAiService;
import com.Ai.dto.TicketIntent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    Long starttime = (long) System.currentTimeMillis();
    private final OpenAiService openAiService;

    public AiController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }
    // 添加票务意图检测的 Pattern（支持多行 JSON）
    private static final Pattern TICKET_PATTERN = Pattern.compile(
            "__TICKET_INTENT__\\s*(\\{[\\s\\S]*?\\})\\s*__TICKET_INTENT_END__");

    private final ObjectMapper objectMapper = new ObjectMapper();
    // 响应类
    public static class ChatResponse {
        public boolean hasTicketIntent;
        public TicketIntent ticket;
        public String rawReply;
        public String error;
    }


    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String chat(@RequestBody Map<String, Object> body) {
        String chatId = (String) body.get("chatId");
        String message = (String) body.get("message");
        VectorStore vectorStore = (VectorStore) body.get("vectorStore");
        return openAiService.excute(message, chatId);
    }
    // 【新增】票务意图检测接口，返回 JSON
    @PostMapping(value = "/chat/process", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> processChat(@RequestBody Map<String, Object> body) {
        String chatId = (String) body.get("chatId");
        String message = (String) body.get("message");

        ChatResponse resp = new ChatResponse();

        String reply;
        try {
            reply = openAiService.excute(message, chatId);
        } catch (Exception e) {
            resp.hasTicketIntent = false;
            resp.rawReply = null;
            resp.error = "LLM 调用出错: " + e.getMessage();
            return ResponseEntity.status(500).body(resp);
        }

        resp.rawReply = reply;

        Matcher m = TICKET_PATTERN.matcher(reply);
        if (!m.find()) {
            resp.hasTicketIntent = false;
            return ResponseEntity.ok(resp);
        }

        String jsonPart = m.group(1);
        try {
            TicketIntent ticket = objectMapper.readValue(jsonPart, TicketIntent.class);
            resp.hasTicketIntent = true;
            resp.ticket = ticket;
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.hasTicketIntent = false;
            resp.error = "票务意图 JSON 解析失败: " + ex.getMessage();
            return ResponseEntity.ok(resp);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("ok", true, "msg", "AI service is up"));
    }
}

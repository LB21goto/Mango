// src/main/java/com/example/controller/AiController.java
package com.Ai.Controller;

import com.Ai.OpenAiService;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OpenAiService openAiService;

    public AiController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String chat(@RequestBody Map<String, Object> body) {
        String chatId = (String) body.get("chatId");
        String message = (String) body.get("message");
        VectorStore vectorStore = (VectorStore) body.get("vectorStore");
        return openAiService.chatOnce(message, chatId);
    }


    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("ok", true, "msg", "AI service is up"));
    }
}

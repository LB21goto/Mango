package com.Ai.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/asr")
public class AsrController {

    private static final Map<String, Object> asrState = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    static {
        com.alibaba.dashscope.utils.Constants.baseWebsocketApiUrl = "wss://dashscope.aliyuncs.com/api-ws/v1/inference";
        asrState.put("isRecording", new AtomicBoolean(false));
        asrState.put("lastText", new AtomicReference<String>(""));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        AtomicBoolean isRecording = (AtomicBoolean) asrState.get("isRecording");
        AtomicReference<String> lastText = (AtomicReference<String>) asrState.get("lastText");
        response.put("success", true);
        response.put("isRecording", isRecording.get());
        response.put("lastText", lastText.get());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startRecording() {
        Map<String, Object> response = new HashMap<>();

        try {
            AtomicBoolean isRecording = (AtomicBoolean) asrState.get("isRecording");

            if (isRecording.get()) {
                response.put("success", false);
                response.put("error", "已经在录音中");
                return ResponseEntity.ok(response);
            }

            isRecording.set(true);
            AtomicReference<String> lastText = (AtomicReference<String>) asrState.get("lastText");
            lastText.set("");

            executorService.submit(() -> {
                try {
                    runRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isRecording.set(false);
                }
            });

            response.put("success", true);
            response.put("message", "录音已开始，请对着麦克风说话");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopRecording() {
        Map<String, Object> response = new HashMap<>();

        try {
            AtomicBoolean isRecording = (AtomicBoolean) asrState.get("isRecording");
            AtomicReference<String> lastText = (AtomicReference<String>) asrState.get("lastText");

            if (!isRecording.get()) {
                response.put("success", false);
                response.put("error", "没有正在录音");
                return ResponseEntity.ok(response);
            }

            isRecording.set(false);
            response.put("success", true);
            response.put("message", "录音已停止");
            response.put("text", lastText.get());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/result")
    public ResponseEntity<Map<String, Object>> getResult() {
        Map<String, Object> response = new HashMap<>();
        AtomicReference<String> lastText = (AtomicReference<String>) asrState.get("lastText");
        response.put("success", true);
        response.put("text", lastText.get());
        return ResponseEntity.ok(response);
    }

    private void runRecording() {
        try {
            String apiKey = System.getenv("DASHSCOPE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                apiKey = "sk-5cfbfacf36d74191a7a4ea8b99664907";
            }

            AtomicReference<String> lastText = (AtomicReference<String>) asrState.get("lastText");
            AtomicBoolean isRecording = (AtomicBoolean) asrState.get("isRecording");

            com.alibaba.dashscope.audio.asr.recognition.Recognition recognizer =
                new com.alibaba.dashscope.audio.asr.recognition.Recognition();

            com.alibaba.dashscope.audio.asr.recognition.RecognitionParam param =
                com.alibaba.dashscope.audio.asr.recognition.RecognitionParam.builder()
                    .model("fun-asr-realtime")
                    .apiKey(apiKey)
                    .format("pcm")
                    .sampleRate(16000)
                    .build();

            StringBuilder fullText = new StringBuilder();

            com.alibaba.dashscope.common.ResultCallback<
                com.alibaba.dashscope.audio.asr.recognition.RecognitionResult> callback =
                new com.alibaba.dashscope.common.ResultCallback<com.alibaba.dashscope.audio.asr.recognition.RecognitionResult>() {

                @Override
                public void onEvent(com.alibaba.dashscope.audio.asr.recognition.RecognitionResult result) {
                    if (result.getSentence() != null && result.getSentence().getText() != null) {
                        String text = result.getSentence().getText();
                        if (result.isSentenceEnd()) {
                            fullText.append(text);
                            lastText.set(fullText.toString());
                            System.out.println("[ASR] Sentence End: " + text);
                        } else {
                            lastText.set(fullText.toString() + text);
                            System.out.println("[ASR] Intermediate: " + text);
                        }
                    }
                }

                @Override
                public void onComplete() {
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("[ASR] Error: " + e.getMessage());
                }
            };

            System.out.println("[ASR] Starting recognition...");

            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            recognizer.call(param, callback);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int maxIterations = 3000;
            int iterations = 0;

            while (isRecording.get() && iterations < maxIterations) {
                int read = targetDataLine.read(buffer.array(), 0, buffer.capacity());
                if (read > 0) {
                    buffer.limit(read);
                    recognizer.sendAudioFrame(buffer);
                    buffer = ByteBuffer.allocate(1024);
                }
                Thread.sleep(20);
                iterations++;
            }

            targetDataLine.stop();
            targetDataLine.close();
            recognizer.stop();

            System.out.println("[ASR] Recording stopped. Final text: " + fullText);
            lastText.set(fullText.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
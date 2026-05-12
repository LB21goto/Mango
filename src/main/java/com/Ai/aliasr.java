package com.Ai;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionResult;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.utils.Constants;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class aliasr {
    private static final AtomicReference<String> lastRecognizedText = new AtomicReference<>("");
    private static final AtomicBoolean isRecording = new AtomicBoolean(false);
    private static final List<String> transcriptionHistory = new CopyOnWriteArrayList<>();

    public static String getLastRecognizedText() {
        return lastRecognizedText.get();
    }

    public static boolean isRecording() {
        return isRecording.get();
    }

    public static List<String> getTranscriptionHistory() {
        return new ArrayList<>(transcriptionHistory);
    }

    public static void main(String[] args) throws InterruptedException {
        Constants.baseWebsocketApiUrl = "wss://dashscope.aliyuncs.com/api-ws/v1/inference";
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new RealtimeRecognitionTask());
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        System.exit(0);
    }

    public static void startRecording() {
        isRecording.set(true);
        System.out.println("[ASR] Recording started");
    }

    public static void stopRecording() {
        isRecording.set(false);
        System.out.println("[ASR] Recording stopped");
    }

    public static void clearHistory() {
        transcriptionHistory.clear();
        lastRecognizedText.set("");
    }

    public static class RealtimeRecognitionTask implements Runnable {
        private final AtomicBoolean isRunning = new AtomicBoolean(true);
        private Recognition recognizer;

        public RealtimeRecognitionTask() {
        }

        @Override
        public void run() {
            System.out.println("[ASR] Initializing recognition service...");
            System.out.println("[ASR] Model: fun-asr-realtime");
            System.out.println("[ASR] WebSocket URL: " + Constants.baseWebsocketApiUrl);

            RecognitionParam param = RecognitionParam.builder()
                    .model("fun-asr-realtime")
                    .apiKey("sk-5cfbfacf36d74191a7a4ea8b99664907")
                    .format("pcm")
                    .sampleRate(16000)
                    .build();

            recognizer = new Recognition();

            ResultCallback<RecognitionResult> callback = new ResultCallback<RecognitionResult>() {
                @Override
                public void onEvent(RecognitionResult result) {
                    String text = result.getSentence().getText();
                    if (text != null && !text.trim().isEmpty()) {
                        if (result.isSentenceEnd()) {
                            lastRecognizedText.set(text);
                            transcriptionHistory.add(text);
                            System.out.println("[ASR] Final: " + text);
                        } else {
                            System.out.println("[ASR] Intermediate: " + text);
                        }
                    }
                }

                @Override
                public void onComplete() {
                    System.out.println("[ASR] Recognition service completed");
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("[ASR] Error occurred: " + e.getMessage());
                    System.err.println("[ASR] Error type: " + e.getClass().getName());
                    e.printStackTrace();
                }
            };

            try {
                System.out.println("[ASR] Starting recognition call...");
                recognizer.call(param, callback);

                System.out.println("[ASR] Opening audio device...");
                AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
                TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
                targetDataLine.open(audioFormat);
                targetDataLine.start();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                isRecording.set(true);
                System.out.println("[ASR] Recording started, will stop in 50 seconds...");
                System.out.println("[ASR] Please start speaking...");

                long start = System.currentTimeMillis();
                int frameCount = 0;
                while (isRunning.get() && System.currentTimeMillis() - start < 50000) {
                    if (isRecording.get()) {
                        int read = targetDataLine.read(buffer.array(), 0, buffer.capacity());
                        if (read > 0) {
                            frameCount++;
                            if (frameCount % 50 == 0) {
                                System.out.println("[ASR] Sent " + frameCount + " audio frames");
                            }
                            buffer.limit(read);
                            recognizer.sendAudioFrame(buffer);
                            buffer = ByteBuffer.allocate(1024);
                        }
                    }
                    Thread.sleep(20);
                }

                System.out.println("[ASR] Total audio frames sent: " + frameCount);
                targetDataLine.stop();
                targetDataLine.close();
                recognizer.stop();
                System.out.println("[ASR] Recording stopped gracefully");

            } catch (Exception e) {
                System.err.println("[ASR] Exception during recognition: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (recognizer != null) {
                    try {
                        recognizer.getDuplexApi().close(1000, "bye");
                    } catch (Exception e) {
                        System.err.println("[ASR] Error closing connection: " + e.getMessage());
                    }
                }
                isRunning.set(false);
            }

            System.out.println("[ASR] Final recognized text: " + lastRecognizedText.get());
            System.out.println("[ASR] Transcription history size: " + transcriptionHistory.size());
            System.out.println("[Metric] requestId: " + recognizer.getLastRequestId()
                    + ", first package delay ms: " + recognizer.getFirstPackageDelay()
                    + ", last package delay ms: " + recognizer.getLastPackageDelay());
        }
    }
}
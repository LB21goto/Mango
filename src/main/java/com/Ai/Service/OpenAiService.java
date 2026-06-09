package com.Ai;


import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Ai.AopLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class OpenAiService {

    @Resource
    private ChatClient chatClient;
    @Resource
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private AiserviceImpl aiserviceimpl;
    @Autowired
    private Mcptools mcptools;
    @Autowired(required = false)
    private List<ToolCallbackProvider> mcpToolCallbackProvider;
    @Autowired
    private IntentRouterService intentRouterService;
    @Autowired
    private LLMSingleFlightCache singleFlightCache;
    // 1. 创建 RAG 顾问，指定使用哪个向量库

    @AopLog
    public String excute(String message, String chatId) {
        long startTime = System.currentTimeMillis();
        System.out.println("【JARVIS】收到消息: " + message);

        // ===== Single Flight 防重逻辑开始 =====
        // 1. 先检查是否有缓存结果
        String cachedResult = singleFlightCache.getCachedResult(message);
        if (cachedResult != null) {
            long t0 = System.currentTimeMillis();
            System.out.println("【JARVIS】缓存命中，耗时: " + (t0 - startTime) + "ms");
            return cachedResult;
        }

        // 2. 获取或创建 CompleteFuture
        CompletableFuture<String> future = singleFlightCache.getOrCreateFuture(message);

        // 3. 如果获得了锁（future 是新创建的），则执行 LLM 调用
        // 如果未获得锁，直接 join 等待结果
        if (!future.isDone()) {
            // 当前线程获得了锁，需要执行 LLM 调用
            try {
                String result = chatOnce(message, chatId, startTime);
                singleFlightCache.cacheResult(message, result);
                return result;
            } catch (Exception e) {
                singleFlightCache.cleanupOnError(message, e);
                throw e;
            }
        } else {
            // 当前线程未获得锁，等待其他线程的结果
            try {
                String result = future.get(35, TimeUnit.SECONDS); // 超时时间稍长于 LOCK_TIMEOUT
                long t0 = System.currentTimeMillis();
                System.out.println("【JARVIS】获得共享结果，耗时: " + (t0 - startTime) + "ms");
                return result;
            } catch (TimeoutException e) {
                System.out.println("【JARVIS】等待超时，执行降级逻辑");
                // 降级：执行自己的 LLM 调用
                try {
                    String result = chatOnce(message, chatId, startTime);
                    return result;
                } catch (Exception ex) {
                    throw new RuntimeException("降级LLM调用失败", ex);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待被中断", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("共享执行异常", e.getCause());
            }
        }
        // ===== Single Flight 防重逻辑结束 =====
    }

    /**
     * 执行真实的 LLM 调用
     */
    public String chatOnce(String message, String chatId, long startTime) {
        // 1. 先硬路由
        RouteType routeType = intentRouterService.classify(message);
        System.out.println("【JARVIS】路由结果: " + routeType);

        QuestionAnswerAdvisor ragAdvisor = new QuestionAnswerAdvisor(vectorStore);
        long t1 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤1-RAG构建: " + (t1 - startTime) + "ms");

        AbstractChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(chatId)
                .build();
        long t2 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤2-Memory构建: " + (t2 - t1) + "ms");

        List<Object> allTools = new ArrayList<>();
        allTools.addAll(mcpToolCallbackProvider);
        long t3 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤3-工具加载: " + (t3 - t2) + "ms");

        String stream = chatClient.prompt()
                .system(tsc.txt)
                .user(message)
                .advisors(memoryAdvisor, ragAdvisor)
                .options(OpenAiChatOptions.builder().model("deepseek-chat").build())
                .tools(allTools.toArray(), mcptools)
                .call()
                .content();
        long t4 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤4-LLM调用: " + (t4 - t3) + "ms");
        System.out.println("【JARVIS】总耗时: " + (t4 - startTime) + "ms");

        return stream;
    }
}

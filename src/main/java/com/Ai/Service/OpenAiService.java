package com.Ai.Service;


import com.Ai.AopLog;
import com.Ai.LLMSingleFlightCache;
import com.Ai.Mcptools;
import com.Ai.RouteType;
import com.Ai.state.ChatStateMachine;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class OpenAiService {
    //https://github.com/java-up-up/super-agent.git
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
    @Autowired
    private ChatStateMachine stateMachine;

    @AopLog
    public String excute(String message, String chatId) {
        long startTime = System.currentTimeMillis();
        String key = (message == null ? "" : message.trim());
        System.out.println("【JARVIS】收到消息: " + message);

        stateMachine.reset();
        stateMachine.start();

        String cachedResult = singleFlightCache.getCachedResult(key);
        if (cachedResult != null) {
            long t0 = System.currentTimeMillis();
            System.out.println("【JARVIS】缓存命中，耗时: " + (t0 - startTime) + "ms");

            return cachedResult;
        }

        LLMSingleFlightCache.Flight flight = singleFlightCache.getOrCreateFuture(key);

        if (flight.isLeader) {
            System.out.println("【JARVIS】我是 leader，开始执行 LLM 调用");
            try {
                String result = chatOnce(message, chatId, startTime);
                singleFlightCache.cacheResult(key, result);
                stateMachine.onComplete();
                return result;
            } catch (Exception e) {
                System.out.println("【JARVIS】leader 执行出错: " + e.getMessage());
                stateMachine.onError(e);
                singleFlightCache.cleanupOnError(key, e);
                throw e;
            }
        } else {
            System.out.println("【JARVIS】我是 follower，等待 leader 完成");
            try {
                String result = flight.future.get(35, TimeUnit.SECONDS);
                long t0 = System.currentTimeMillis();
                System.out.println("【JARVIS】获得共享结果，耗时: " + (t0 - startTime) + "ms");
                stateMachine.onComplete();
                return result;
            } catch (TimeoutException e) {
                System.out.println("【JARVIS】等待超时，执行降级逻辑");
                try {
                    String result = chatOnce(message, chatId, startTime);
                    stateMachine.onComplete();
                    return result;
                } catch (Exception ex) {
                    stateMachine.onError(ex);
                    throw new RuntimeException("降级LLM调用失败", ex);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stateMachine.onError(e);
                throw new RuntimeException("等待被中断", e);
            } catch (ExecutionException e) {
                stateMachine.onError(e);
                throw new RuntimeException("共享执行异常", e.getCause());
            }
        }
    }

    public String chatOnce(String message, String chatId, long startTime) {
        RouteType routeType = intentRouterService.classify(message);
        System.out.println("【JARVIS】路由结果: " + routeType);

        stateMachine.onIntentRecognized(routeType);
        stateMachine.onProcessing();

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
                .system("tsc.txt")
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
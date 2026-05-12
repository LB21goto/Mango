package com.Ai.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class Aiconfig {
    @Value("https://dashscope.aliyuncs.com/compatible-mode")
    private String baseUrl;

    @Value("sk-5cfbfacf36d74191a7a4ea8b99664907")
    private String apiKey;


    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
    // 1. 创建一个纯内存的聊天记录本 (核心！)
    @Bean
    public ChatMemory chatMemory() {
        // InMemoryChatMemory 就是把数据存在 Java 的 List 里，重启就没了
        return new InMemoryChatMemory();
    }
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
    @Bean
    public OpenAiEmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        RetryTemplate retryTemplate = RetryTemplate.defaultInstance();
        return new OpenAiEmbeddingModel(openAiApi,  MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model("text-embedding-v3")
                        .build(),
                retryTemplate);
    }
}
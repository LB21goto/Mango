package com.Ai.config;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

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

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }



    @Bean
    @ConfigurationProperties(prefix = "pgvector.datasource")
    public DataSource pgvectorDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel, DataSource pgvectorDataSource) {
        return PgVectorStore.builder(new JdbcTemplate(pgvectorDataSource), embeddingModel)
                .initializeSchema(true)
                .dimensions(1024)
                .build();
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

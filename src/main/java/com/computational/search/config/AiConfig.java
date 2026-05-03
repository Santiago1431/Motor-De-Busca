package com.computational.search.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaModel) {
        return ChatClient.create(ollamaModel);
    }

    @Bean
    public ChatClient visionChatClient(OllamaChatModel ollamaModel) {
        return ChatClient.builder(ollamaModel)
                .defaultOptions(org.springframework.ai.ollama.api.OllamaChatOptions.builder()
                        .model("llava")
                        .build())
                .build();
    }
}

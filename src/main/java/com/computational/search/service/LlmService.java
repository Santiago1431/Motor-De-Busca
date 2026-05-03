package com.computational.search.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.core.io.ByteArrayResource;

@Slf4j
@Service
public class LlmService {

    private final OllamaChatModel ollamaModel;
    private final ChatClient visionChatClient;

    public LlmService(OllamaChatModel ollamaModel, ChatClient visionChatClient) {
        this.ollamaModel = ollamaModel;
        this.visionChatClient = visionChatClient;
    }

    @Cacheable(value = "llmCache", key = "#latex")
    public String llmProcess(String latex) {
        if (latex == null || latex.trim().isEmpty()) {
            return latex;
        }
        // Prompt extremamente direto para forçar a LLM a não ser prolixa
        String prompt = "SYSTEM: You are a mathematical identifier. Output ONLY the name of the formula. No steps, no reasoning, no chat.\n"
                +
                "USER: Identify the common name of this LaTeX formula in English: " + latex + "\n" +
                "If you don't know the name, return exactly the same formula provided.\n" +
                "RESPONSE:";

        String result = ollamaModel.call(prompt).trim();

        // Limpeza de possíveis artefatos da LLM
        result = result.replaceAll("^[\"']|[\"']$", "") // Remove aspas
                .replaceAll("\\.$", ""); // Remove ponto final

        log.info("Formula Extraida pela LLM: " + result);

        return result;
    }
}

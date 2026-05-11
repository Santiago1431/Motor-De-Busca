package com.computational.search.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LlmService {

    private static final String FORMULA_IDENTIFIER_PROMPT = 
            "SYSTEM: You are a mathematical identifier. Output ONLY the name of the formula. No steps, no reasoning, no chat.\n" +
            "USER: Identify the common name of this LaTeX formula in English: %s\n" +
            "If you don't know the name, return exactly the same formula provided.\n" +
            "RESPONSE:";

    private final OllamaChatModel ollamaModel;
    private final ChatClient visionChatClient;

    public LlmService(OllamaChatModel ollamaModel, ChatClient visionChatClient) {
        this.ollamaModel = ollamaModel;
        this.visionChatClient = visionChatClient;
    }

    @Cacheable(value = "llmCache", key = "#latex")
    public String identifyFormulaName(String latex) {
        if (latex == null || latex.trim().isEmpty()) {
            return latex;
        }

        try {
            String prompt = String.format(FORMULA_IDENTIFIER_PROMPT, latex);
            String result = ollamaModel.call(prompt).trim();

            String cleanedResult = cleanLlmOutput(result);
            log.info("Formula identified by LLM: {}", cleanedResult);

            return cleanedResult;
        } catch (Exception e) {
            log.warn("Failed to identify formula name for latex: {}. Error: {}", latex, e.getMessage());
            return latex;
        }
    }

    private String cleanLlmOutput(String output) {
        return output.replaceAll("^[\"']|[\"']$", "") // Remove surrounding quotes
                     .replaceAll("\\.$", "");         // Remove trailing period
    }
}

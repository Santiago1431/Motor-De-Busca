package com.computational.search.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LlmService {

    private final OllamaChatModel model;

    public LlmService(OllamaChatModel model) {
        this.model = model;
    }

    public String llmProcess(String latex) {
        // Prompt extremamente direto para forçar a LLM a não ser prolixa
        String prompt = "SYSTEM: You are a mathematical identifier. Output ONLY the name of the formula. No steps, no reasoning, no chat.\n" +
                        "USER: Identify the common name of this LaTeX formula in English: " + latex + "\n" +
                        "If you don't know the name, return exactly the same formula provided.\n" +
                        "RESPONSE:";

        String result = model.call(prompt).trim();

        // Limpeza de possíveis artefatos da LLM
        result = result.replaceAll("^[\"']|[\"']$", "") // Remove aspas
                       .replaceAll("\\.$", "");         // Remove ponto final

        log.info("Formula Extraida pela LLM: " + result);

        return result;
    }
}

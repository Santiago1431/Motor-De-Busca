package com.computational.search.controller;

import lombok.extern.slf4j.Slf4j;

import com.computational.search.api.facade.SearchApi;
import com.computational.search.api.model.Result;
import com.computational.search.api.model.SearchResponse;
import com.computational.search.service.LlmService;
import com.computational.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.computational.search.service.ExtractTextService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@CrossOrigin
@RestController
public class SearchController implements SearchApi {

    private final SearchService searchService;
    private final LlmService llmService;
    private final ExtractTextService extractTextService;
    

    public SearchController(SearchService searchService, LlmService llmService, ExtractTextService extractTextService) {
        this.searchService = searchService;
        this.llmService = llmService;
        this.extractTextService = extractTextService;
    }

    @Override
    public CompletableFuture<ResponseEntity<SearchResponse>> search(String query, Integer page) {
        // Inicia o processamento da LLM em paralelo com um timeout rigoroso
        CompletableFuture<String> llmFuture = CompletableFuture.supplyAsync(() -> llmService.llmProcess(query))
                .completeOnTimeout(null, 1500, java.util.concurrent.TimeUnit.MILLISECONDS) // Máximo 1.5s
                .exceptionally(ex -> null); // Se der erro, ignora e segue

        return llmFuture.thenApply(latexName -> {
            var result = searchService.submitQuery(query, latexName, page);
            return ResponseEntity.ok(result);
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<SearchResponse>> searchByImage(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Recebida imagem para busca (LatexOCR): " + file.getOriginalFilename());
                
                // 1. Extrai o LaTeX da imagem via Python LatexOCR Service
                String latex = extractTextService.extractTextFromImage(file);
                log.info("LaTeX extraído: " + latex);
                
                if (latex == null || latex.trim().isEmpty()) {
                    return ResponseEntity.ok(new SearchResponse());
                }

                // 2. Tenta identificar o nome da fórmula com timeout (máximo 5s para imagem)
                String finalLatex = latex;
                String identifiedName = null;
                try {
                    identifiedName = CompletableFuture.supplyAsync(() -> llmService.llmProcess(finalLatex))
                        .get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("Falha ou timeout ao identificar nome da fórmula pela LLM: " + e.getMessage());
                }
                
                // 3. Realiza a busca no Elasticsearch (usa o nome se identificado, ou apenas o latex)
                var result = searchService.submitQuery(latex, identifiedName, 1);
                
                return ResponseEntity.ok(result);
            } catch (IOException e) {
                log.error("Erro ao processar imagem", e);
                throw new RuntimeException("Falha ao ler o arquivo de imagem enviado.");
            }
        }).orTimeout(60, TimeUnit.SECONDS);
    }

    @GetMapping("/chat")
    public String testLLM(@RequestParam String formula) {
        return llmService.llmProcess(formula);
    }



    @PostMapping("/tess")
    public ResponseEntity<String> extractTextFromImage(@RequestBody MultipartFile file) throws IOException {
        return ResponseEntity.ok(extractTextService.extractTextFromImage(file));
    }
}

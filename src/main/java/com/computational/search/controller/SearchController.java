package com.computational.search.controller;

import com.computational.search.api.facade.SearchApi;
import com.computational.search.api.model.Result;
import com.computational.search.api.model.SearchResponse;
import com.computational.search.service.LlmService;
import com.computational.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CrossOrigin
@RestController
public class SearchController implements SearchApi {

    private final SearchService searchService;
    private final LlmService llmService;

    public SearchController(SearchService searchService, LlmService llmService) {
        this.searchService = searchService;
        this.llmService = llmService;
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


    @GetMapping("/chat")
    public String testLLM (@RequestParam String formula){
        return llmService.llmProcess(formula);
    }
}

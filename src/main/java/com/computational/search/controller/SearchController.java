package com.computational.search.controller;

import com.computational.search.api.facade.SearchApi;
import com.computational.search.api.model.SearchResponse;
import com.computational.search.service.SearchOrchestratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@CrossOrigin
@RestController
public class SearchController implements SearchApi {

    private final SearchOrchestratorService searchOrchestrator;

    public SearchController(SearchOrchestratorService searchOrchestrator) {
        this.searchOrchestrator = searchOrchestrator;
    }

    @Override
    public CompletableFuture<ResponseEntity<SearchResponse>> search(String query, Integer page) {
        log.info("Processing search query: {}, page: {}", query, page);
        return searchOrchestrator.performSearch(query, page)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<SearchResponse>> searchByImage(MultipartFile file) {
        log.info("Processing image search for file: {}", file.getOriginalFilename());
        return searchOrchestrator.performImageSearch(file)
                .thenApply(ResponseEntity::ok)
                .orTimeout(60, TimeUnit.SECONDS);
    }
}

package com.computational.search.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.computational.search.api.model.Result;
import com.computational.search.api.model.SearchResponse;
import com.computational.search.domain.EsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private final EsClient esClient;

    public SearchService(EsClient esClient) {
        this.esClient = esClient;
    }

    public SearchResponse submitQuery(String query, String identifiedName, Integer page) {
        var searchResponse = esClient.search(query, identifiedName, page);
        List<Hit<ObjectNode>> hits = searchResponse.hits().hits();
        long totalHits = searchResponse.hits().total().value();
        int totalPages = (int) Math.ceil((double) totalHits / DEFAULT_PAGE_SIZE);

        List<Result> resultsList = hits.stream()
                .map(this::mapToResult)
                .collect(Collectors.toList());

        return new SearchResponse()
                .results(resultsList)
                .query(query)
                .totalPages(totalPages);
    }

    private Result mapToResult(Hit<ObjectNode> hit) {
        ObjectNode source = hit.source();
        if (source == null) return new Result();

        // Check if we have highlights, otherwise use raw content
        String rawContent = extractHighlightedContent(hit).orElse(getTextValue(source, "content"));
        
        // Apply safe formatting and truncation (only truncate if NOT a highlight)
        boolean shouldTruncate = !hit.highlight().containsKey("content");
        String safeContent = safeLatexFormat(rawContent, shouldTruncate);

        Result result = new Result()
                .title(getTextValue(source, "title"))
                .url(getTextValue(source, "url"))
                .abs(safeContent);

        if (source.has("formulas_latex")) {
            List<String> formulas = new ArrayList<>();
            source.get("formulas_latex").forEach(node -> formulas.add(node.asText()));
            result.setFormulasLatex(formulas);
        }

        return result;
    }

    private Optional<String> extractHighlightedContent(Hit<ObjectNode> hit) {
        if (hit.highlight().containsKey("content")) {
            List<String> fragments = hit.highlight().get("content");
            if (!fragments.isEmpty()) {
                return Optional.of(String.join(" ... ", fragments));
            }
        }
        return Optional.empty();
    }

    private String safeLatexFormat(String content, boolean truncate) {
        if (content == null) return "";
        
        // 1. Convert tags to delimiters
        String formatted = content.replaceAll("<(math|som)\\d*>", "\\$")
                                  .replaceAll("</(math|som)\\d*>", "\\$");
        
        formatted = formatted.replaceAll("\\s+", " ").trim();
        
        if (truncate && formatted.length() > 300) {
            formatted = formatted.substring(0, 297).trim() + "...";
        }
        
        // 4. CRITICAL: Fix unbalanced delimiters ($) and braces ({})
        long dollarCount = formatted.chars().filter(ch -> ch == '$').count();
        if (dollarCount % 2 != 0) {
            // Find the last opened $ and balance braces within it
            int lastDollar = formatted.lastIndexOf('$');
            String formulaPart = formatted.substring(lastDollar + 1);
            
            long openBraces = formulaPart.chars().filter(ch -> ch == '{').count();
            long closeBraces = formulaPart.chars().filter(ch -> ch == '}').count();
            
            // Add missing closing braces
            for (int i = 0; i < (openBraces - closeBraces); i++) {
                formatted += "}";
            }
            
            formatted = formatted + "$";
        }
        
        return formatted;
    }

    private String getTextValue(ObjectNode node, String fieldName) {
        return Optional.ofNullable(node.get(fieldName))
                .map(JsonNode::asText)
                .orElse("");
    }
}

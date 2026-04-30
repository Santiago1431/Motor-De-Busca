package com.computational.search.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.computational.search.api.model.Result;
import com.computational.search.api.model.SearchResponse;
import com.computational.search.domain.EsClient;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final EsClient esClient;

    public SearchService(EsClient esClient) {
        this.esClient = esClient;
    }

    public com.computational.search.api.model.SearchResponse submitQuery(String query, Integer page) {
        var searchResponse = esClient.search(query, page);
        List<Hit<ObjectNode>> hits = searchResponse.hits().hits();
        long totalHits = searchResponse.hits().total().value();
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalHits / pageSize);

        var resultsList = hits.stream().map(h -> {
                    Result r = new Result()
                            .abs(treatContent(h.source().get("content").asText()))
                            .title(h.source().get("title").asText())
                            .url(h.source().get("url").asText());

                    if (h.source().has("formulas_latex")) {
                        java.util.List<String> formulas = new java.util.ArrayList<>();
                        h.source().get("formulas_latex").forEach(node -> formulas.add(node.asText()));
                        r.setFormulasLatex(formulas);
                    }
                    return r;
                }
        ).collect(Collectors.toList());

        return new SearchResponse()
                .results(resultsList)
                .totalPages(totalPages);
    }

    private String treatContent(String content) {
        // Remove specific tags that might be indexed from raw XML/HTML
        content = content.replaceAll("</?(som|math)\\d*>", "");
        
        // Instead of removing everything that isn't A-Za-z, let's just clean up excessive whitespace
        // and keep the content as is to preserve LaTeX and technical terms.
        content = content.replaceAll("\\s+", " ");
        content = content.trim();
        
        // Optional: truncate if too long for a summary
        if (content.length() > 300) {
            content = content.substring(0, 297) + "...";
        }
        
        return content;
    }
}

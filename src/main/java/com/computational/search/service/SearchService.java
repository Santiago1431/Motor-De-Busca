package com.computational.search.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.computational.search.api.model.Result;
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

    public List<Result> submitQuery(String query, Integer page) {
        var searchResponse = esClient.search(query, page);
        List<Hit<ObjectNode>> hits = searchResponse.hits().hits();

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

        return resultsList;
    }

    private String treatContent(String content) {
        content = content.replaceAll("</?(som|math)\\d*>", "");
        content = content.replaceAll("[^A-Za-z\\s]+", "");
        content = content.replaceAll("\\s+", " ");
        content = content.replaceAll("^\\s+", "");
        return content;
    }
}

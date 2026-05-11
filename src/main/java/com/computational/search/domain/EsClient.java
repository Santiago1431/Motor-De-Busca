package com.computational.search.domain;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.altindag.ssl.SSLFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EsClient {
    private ElasticsearchClient elasticsearchClient;

    public EsClient(
            @Value("${environments.elastic.elasticPWD}") String pwd,
            @Value("${environments.elastic.host:localhost}") String host,
            @Value("${environments.elastic.port:9200}") int port,
            @Value("${environments.elastic.protocol:https}") String protocol) {
        createConnection(pwd, host, port, protocol);
    }

    private void createConnection(String pwd, String host, int port, String protocol) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        String USER = "elastic";

        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(USER, pwd));

        SSLFactory sslFactory = SSLFactory.builder()
                .withUnsafeTrustMaterial()
                .withUnsafeHostnameVerifier()
                .build();

        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, protocol))
                .setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslFactory.getSslContext())
                        .setSSLHostnameVerifier(sslFactory.getHostnameVerifier()))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper());

        elasticsearchClient = new co.elastic.clients.elasticsearch.ElasticsearchClient(transport);
    }

    public SearchResponse search(String query, String identifiedName, Integer page) {
        int pageSize = 10;
        int from = ((page != null ? page : 1) - 1) * pageSize;

        boolean isQuoted = query.startsWith("\"") && query.endsWith("\"");
        String processedQuery = isQuoted ? query.substring(1, query.length() - 1) : query;

        Query esQuery = Query.of(q -> q
                .bool(b -> {
                    // 1. Keyword Match (Base relevance)
                    b.should(s -> s
                        .multiMatch(mm -> mm
                            .fields("formulas_latex", "content", "title")
                            .query(processedQuery)
                            .boost(1.0f)
                        )
                    );

                    // 2. Phrase Match in Title (High boost)
                    b.should(s -> s
                        .matchPhrase(mp -> mp
                            .field("title")
                            .query(processedQuery)
                            .boost(100.0f)
                            .slop(2) // Allow some flexibility
                        )
                    );

                    // 3. Phrase Match in Content/Resumo (Medium boost)
                    b.should(s -> s
                        .matchPhrase(mp -> mp
                            .field("content")
                            .query(processedQuery)
                            .boost(50.0f)
                            .slop(3)
                        )
                    );

                    // 4. Boost results with identified formula name from LLM
                    if (identifiedName != null && !identifiedName.trim().isEmpty()) {
                        b.should(s -> s
                            .match(ma -> ma
                                .field("title")
                                .query(identifiedName)
                                .operator(Operator.And)
                                .boost(150.0f)
                            )
                        );
                    }

                    // If quoted, force phrase match (must instead of should)
                    if (isQuoted) {
                        b.minimumShouldMatch("100%"); // Effectively turns should into must for phrases
                    }

                    return b;
                })
        );

        SearchResponse<ObjectNode> response;
        try {
            response = elasticsearchClient.search(s -> s
                    .index("wikipedia")
                    .from(from)
                    .size(pageSize)
                    .query(esQuery)
                    .highlight(h -> h
                        .fields("content", f -> f
                            .preTags("<em>")
                            .postTags("</em>")
                        )
                    )
                    .collapse(c -> c.field("url.keyword")),
                ObjectNode.class);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search failed", e);
        }

        return response;
    }
}

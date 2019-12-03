package com.github.hcsp.controller;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ElasticSearchEngine {
    public static void main(String[] args) throws IOException {
        System.out.println("Please input what you want search:");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String keyword = bufferedReader.readLine();
        search(keyword);
    }

    public static void search(String keyword) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")))

        ) {
            SearchRequest searchRequest = new SearchRequest("news");
            searchRequest.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "content", "title")));
            SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
            search.getHits().forEach(documentFields -> System.out.println(documentFields.getSourceAsString()));
        }
    }
}

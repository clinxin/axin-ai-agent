package com.clinxin.axinaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PGVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorStoreConfig() {
        List<Document> documents = List.of(
                new Document("axin 的计划报告有哪些建议？我需要关注哪些方面？", Map.of("meta1", "meta1")),
                new Document("axin 的计划管家：给您提供最匹配您需求的规划指南"),
                new Document("axin 今天没有钓鱼.", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore
                .similaritySearch(SearchRequest.builder().query("怎么做计划").topK(3).build());
        Assertions.assertNotNull(results);
    }
}

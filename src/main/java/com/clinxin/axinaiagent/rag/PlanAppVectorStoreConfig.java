package com.clinxin.axinaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 计划大师向量数据库配置（初始化基于内存的向量数据库）
 */
@Configuration
public class PlanAppVectorStoreConfig {

    @Resource
    private PlanAppDocumenyLoader planAppDocumenyLoader;

    @Bean
    VectorStore planAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore
                .builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档
        List<Document> documents = planAppDocumenyLoader.loadMarkdownDocuments();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}



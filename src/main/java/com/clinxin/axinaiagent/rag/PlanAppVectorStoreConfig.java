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

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore planAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore
                .builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档
        List<Document> documents = planAppDocumenyLoader.loadMarkdownDocuments();
        // 自主切分文档
        // List<Document> splitDocument = myTokenTextSplitter.splitCustomized(documents);
        // 自动补充关键词元信息
        List<Document> enrichDocuments = myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichDocuments);
        return simpleVectorStore;
    }
}



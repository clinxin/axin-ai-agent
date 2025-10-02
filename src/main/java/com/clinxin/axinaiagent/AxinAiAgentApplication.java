package com.clinxin.axinaiagent;

import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {OllamaAutoConfiguration.class, PgVectorStoreAutoConfiguration.class})
public class AxinAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxinAiAgentApplication.class, args);
    }

}

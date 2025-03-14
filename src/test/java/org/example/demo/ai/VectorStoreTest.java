package org.example.demo.ai;

import org.example.demo.SpringBootTestWithProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWithProfile("test")
public class VectorStoreTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @TestConfiguration
    static class VectorStoreTestConfig {
        @Bean
        public VectorStore vectorStore(EmbeddingModel embeddingModel) {
            return new SimpleVectorStore(embeddingModel);
        }
    }

    @Test
    void testVectorStoreSimilaritySearch() {
        // Create sample documents
        List<Document> documents = List.of(
                new Document("Spring AI is a framework that simplifies AI integration in Spring applications."),
                new Document("Spring Boot makes it easy to create stand-alone applications."),
                new Document("Vector databases store and search vector embeddings efficiently.")
        );

        // Add documents to vector store
        vectorStore.add(documents);

        // Perform similarity search
        String query = "What is Spring AI?";
        List<Document> results = vectorStore.similaritySearch(query);

        // Verify results
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getContent().contains("Spring AI"));

    }
}



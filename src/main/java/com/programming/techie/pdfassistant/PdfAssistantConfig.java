package com.programming.techie.pdfassistant;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Configuration
public class PdfAssistantConfig {
    @Value("${temperature}")
    private Double temperature;
    @Value("${doc_path}")
    private String doc_path;
    @Value("${llm}")
    private String llm;

    @Value("${ollama_url}")
    private String ollama_url;

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        Path path = Paths.get(doc_path+"/data.json");
        return Files.exists(path) ? InMemoryEmbeddingStore.fromFile(path): new InMemoryEmbeddingStore<>();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor() {

        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(100, 0))
                .embeddingModel(embeddingModel())
                .embeddingStore(embeddingStore())
                .build();
    }

    @Bean
    public ConversationalRetrievalChain conversationalRetrievalChain() {
        return ConversationalRetrievalChain.builder()
                .chatLanguageModel(OllamaChatModel.builder()
                        .temperature(temperature)
                        .timeout(Duration.ofMinutes(10))
                        .logRequests(true)
                        .logResponses(true)
                        .baseUrl(ollama_url)
                        .modelName(llm)
                        .build())
                .contentRetriever(contentRetriever(embeddingStore(),embeddingModel()))
                .build();
    }
    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

        // You will need to adjust these parameters to find the optimal setting, which will depend on two main factors:
        // - The nature of your data
        // - The embedding model you are using
        int maxResults = 1;
        double minScore = 0.6;

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }
}

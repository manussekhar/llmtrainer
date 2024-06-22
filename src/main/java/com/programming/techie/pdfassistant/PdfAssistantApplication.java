package com.programming.techie.pdfassistant;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@SpringBootApplication
public class PdfAssistantApplication {
    private final EmbeddingStoreIngestor embeddingStoreIngestor;

    @Value("${doc_path}")
    private String doc_path;

    public PdfAssistantApplication(EmbeddingStoreIngestor embeddingStoreIngestor) {
        this.embeddingStoreIngestor = embeddingStoreIngestor;
    }

    @PostConstruct
    public void init() {
        List<Document> documents = loadDocuments(
                Paths.get(doc_path),
                new ApacheTikaDocumentParser());
        embeddingStoreIngestor.ingest(documents);
    }


    public static void main(String[] args) {
        SpringApplication.run(PdfAssistantApplication.class, args);
    }

}

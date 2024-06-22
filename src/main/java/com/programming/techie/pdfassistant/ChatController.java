package com.programming.techie.pdfassistant;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    @Value("${doc_path}")
    private String doc_path;
    private final ConversationalRetrievalChain conversationalRetrievalChain;
    private final EmbeddingStoreIngestor embeddingStoreIngestor;

    @PostMapping
    @RequestMapping("/chat")
    public String chat(@RequestBody String question) {
	log.debug("Question is - {}", question);
        var answer = conversationalRetrievalChain.execute(question);
        log.debug("Answer is - {}", answer);
        return answer;
    }

    @PostMapping
    @RequestMapping("/train")
    public void train(@RequestBody String url) {
        log.debug("Training started");
        log.debug("Fetching documents started");
        //run the tool
        log.debug("Fetching documents finished");
        log.debug("Saving documents to database");
        List<Document> documents = loadDocuments(
                Paths.get(doc_path),
                new ApacheTikaDocumentParser());
        embeddingStoreIngestor.ingest(documents);
        log.debug("Training finished");
    }


}

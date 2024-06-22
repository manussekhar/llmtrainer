package com.manu.myna.llmtrainer;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    String regex = "^fetch\\s+.*(https?://[\\w.-]+(?:\\.[\\w\\.-]+)+(?:[/?#]\\S*)?)$";
    Pattern pattern = Pattern.compile(regex);

    @PostMapping
    @RequestMapping("/chat")
    public String chat(@RequestBody String question) {
        Matcher matcher = pattern.matcher(question);
        if (matcher.matches()) {
            train(matcher.group(1));
            return "Training completed.";
        }
        log.debug("Question is - {}", question);
        var answer = conversationalRetrievalChain.execute(question);
        log.debug("Answer is - {}", answer);
        return answer;
    }


    public void train(String url) {
        log.debug("Training started");
        log.debug("Fetching documents started");
        download(url);
        log.debug("Fetching documents finished");
        log.debug("Saving documents to database");
        List<Document> documents = loadDocuments(
                Paths.get(doc_path),
                new ApacheTikaDocumentParser());
        embeddingStoreIngestor.ingest(documents);
        log.debug("Training finished");
    }


    public void download(String url)  {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Resource> response = restTemplate.getForEntity(url, Resource.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            Resource resource = response.getBody();
            if (resource != null) {
                try {
                    File file = new File(getPathname(url));
                    file.createNewFile();
                    try (InputStream inputStream = resource.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new RuntimeException("Failed to download the file. HTTP Status: " + response.getStatusCode());
        }
    }

    @NotNull
    private String getPathname(String url) {
        return doc_path + "/" + url.replaceAll("^(http://|https://)", "").replace("/", ".")+this.getExtension(url);
    }

    private String getExtension(String url) {
        return url.contains("pdf")?".pdf":".html";
    }


}

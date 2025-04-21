package com.java.rag.pdf;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataLoader {

    private final VectorStore vectorStore;

    private final JdbcClient jdbcClient;

//    @Value("classpath:/waiver-policy2022.pdf")
    @Value("classpath:/sol_support.pdf")
    private Resource pdfResource;

    public DataLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

//    @PostConstruct
//    public void init() {
//        Integer count =
//                jdbcClient.sql("select COUNT(*) from vector_store")
//                        .query(Integer.class)
//                        .single();
//
//        System.out.println("No of Records in the PG Vector Store = " + count);
//
//        if(count == 0) {
//            System.out.println("Loading the file in the PG Vector Store");
//            PdfDocumentReaderConfig config
//                    = PdfDocumentReaderConfig.builder()
//                    .withPagesPerDocument(1)
//                    .build();
//
//            PagePdfDocumentReader reader
//                    = new PagePdfDocumentReader(pdfResource, config);
//
//            var textSplitter = new TokenTextSplitter();
//            vectorStore.accept(textSplitter.apply(reader.get()));
//
//            System.out.println("Application is ready to Serve the Requests");
//        }
//    }




    @PostConstruct
    public void init() {
        Integer count = jdbcClient.sql("select COUNT(*) from vector_store")
                .query(Integer.class)
                .single();

        System.out.println("No of Records in the PG Vector Store = " + count);

        if(count == 0) {
            System.out.println("Loading the file in the PG Vector Store");

            // Configure PDF reader
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);

            // Process and clean the documents
            List<Document> documents = reader.get().stream()
                    .map(doc -> {
                        // Clean null bytes and other problematic characters
                        String cleanedContent = doc.getText()
                                .replace("\u0000", "") // Remove null bytes
                                .replaceAll("\\p{C}", ""); // Remove all control characters

                        return new Document(doc.getId(), cleanedContent, doc.getMetadata());
                    })
                    .collect(Collectors.toList());

            var textSplitter = new TokenTextSplitter();
            vectorStore.accept(textSplitter.apply(documents));

            System.out.println("Application is ready to Serve the Requests");
        }
    }
}
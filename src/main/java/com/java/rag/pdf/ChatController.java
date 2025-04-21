package com.java.rag.pdf;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final QuestionAnswerAdvisor qaAdvisor;

    public ChatController(ChatClient.Builder builder,
                          VectorStore vectorStore) {
        this.vectorStore = vectorStore;

        // Configure the QA advisor with proper semantic search parameters
        this.qaAdvisor = new QuestionAnswerAdvisor(vectorStore,
                SearchRequest.builder()
                        .topK(5) // Get top 5 most relevant chunks
                        .similarityThreshold(0.75) // Slightly lower threshold for better recall
                        .build());

        this.chatClient = builder
                .defaultAdvisors(qaAdvisor)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .build())
                .build();
    }

    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestParam(name = "message", required = true) String message) {

        if (message == null || message.trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please enter a valid question (min 3 characters)"));
        }

        try {
            // 1. First perform semantic search to get relevant context
            List<Document> relevantDocs = vectorStore.similaritySearch(
                    SearchRequest.builder().query(message)
                            .topK(5)
                            .similarityThreshold(0.75)
                            .build()
            );

            // 2. Build the context from retrieved documents
            String context = relevantDocs.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n\n"));

            // 3. Create a prompt with your schema context
            String systemPrompt = """
                You are a SQL expert assistant with deep knowledge of our database schema.
                You have access to these relevant schema definitions:
                
                %s
                
                Rules:
                - Always generate correct SQL for ORACLE
                - If asked for schema info, reference these tables
                - For non-SQL questions, respond naturally
                """.formatted(context);

            // 4. Get the AI response
            String response = chatClient.prompt()
                    .system(systemPrompt) // Provide schema context
                    .user(message)
                    .call()
                    .content();

            return ResponseEntity.ok(Map.of(
                    "response", response,
                    "relevant_docs", relevantDocs.stream()
                            .map(doc -> Map.of(
                                    "content", doc.getText(),
                                    "metadata", doc.getMetadata()
                            ))
                            .collect(Collectors.toList())
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Processing failed: " + e.getMessage()));
        }
    }
}
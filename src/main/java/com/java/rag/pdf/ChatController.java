package com.java.rag.pdf;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    @GetMapping("/")
    public String chat(@RequestParam (name = "message", defaultValue = "") String message) {
        if (message != null && message.length() > 5) {
            return chatClient.prompt()
                    .user(""+message)
                    .call()
                    .content();
        }else{
            return "Please enter a valid question";
        }
    }
}
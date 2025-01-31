package org.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // 채팅 응답
    public String getChatResponse(String userMessage) {
        // 프롬프팅 커스터마이징
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

}

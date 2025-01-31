package org.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceMockTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        chatService = new ChatService(chatClientBuilder);
    }

    @Test
    @DisplayName("정상 응답 테스트")
    void getChatResponse() {
        // given
        String userMessage = "Hello";
        String expectedResponse = "Hi CallBack";

        when(chatClient.prompt().user(userMessage).call().content()).thenReturn(expectedResponse);

        // when
        String actualResponse = chatService.getChatResponse(userMessage);

        // then
        assertEquals(expectedResponse, actualResponse);
    }
}

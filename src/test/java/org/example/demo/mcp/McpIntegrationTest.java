package org.example.demo.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import org.example.demo.SpringBootTestWithProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTestWithProfile("test")
public class McpIntegrationTest {

//    @Autowired
//    List<McpAsyncClient> clients;

    @Autowired
    List<McpSyncClient> syncClients;

    @Autowired
    ChatModel chatModel;
    @Autowired
    private ChatClient.Builder chatClientBuilder;


    @Test
    @DisplayName("Mcp 툴 리스트 테스트") // 테스트 이름: MCP 툴 리스트 관련 기능 테스트
    void testAsyncMcpToolCallBacks() {
        // ChatClient 객체 생성 (SyncMcpToolCallbackProvider를 사용)
        ChatClient chatClient = chatClientBuilder.defaultTools(new SyncMcpToolCallbackProvider(syncClients)).build();

        // 프롬프트 실행 후 반환된 응답 확인
        String response = chatClient.prompt("save content on 1234 .txt").call().content().toString();

        // 응답이 null이 아닌지 확인
        assertNotNull(response, "응답은 null이 아니어야 합니다");
        // 응답에 "content"라는 단어가 포함되어 있는지 확인
        assertTrue(response.contains("content"), "응답에 'content' 단어가 포함되어야 합니다");
    }

    @Test
    @DisplayName("깊은 사고 테스트") // 테스트 이름: Messi 관련 질문 처리 테스트
    void deepThink() {
        // ChatClient 객체 생성 (특정 SyncMcpToolCallbackProvider 사용)
        ChatClient chatClient = chatClientBuilder.defaultTools(new SyncMcpToolCallbackProvider(syncClients.get(1))).build();

        // 프롬프트 실행 후 반환된 응답 확인
        String response = chatClient.prompt("can you explain Lionel Messi's is GOAT is right?").call().content().toString();

        // 응답이 null이 아닌지 확인
        assertNotNull(response, "응답은 null이 아니어야 합니다");
        // 응답에 "Messi"라는 단어가 포함되어 있는지 확인
        assertTrue(response.toLowerCase().contains("messi"), "응답에 'Messi'라는 단어가 포함되어야 합니다");
    }

    @Test
    @DisplayName("웹 검색 테스트") // 테스트 이름: 날짜와 서울 날씨 검색 처리 테스트
    void webSearch() {
        // ChatClient 객체 생성 (SyncMcpToolCallbackProvider를 사용)
        ChatClient chatClient = chatClientBuilder.defaultTools(new SyncMcpToolCallbackProvider(syncClients)).build();

        // 프롬프트 실행 후 반환된 응답 확인
        String response = chatClient.prompt("do you know today date? answer and then Search Today weather in Seoul of Korea").call().content().toString();

        // 응답이 null이 아닌지 확인
        assertNotNull(response, "응답은 null이 아니어야 합니다");
        // 응답에 "Seoul"이라는 단어가 포함되어 있는지 확인
        assertTrue(response.toLowerCase().contains("seoul"), "응답에 'Seoul'이라는 단어가 포함되어야 합니다");
    }
}
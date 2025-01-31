package org.example.demo.service;

import org.example.demo.SpringBootTestWithProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWithProfile("test")
class ChatServiceTest {
    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    @DisplayName("Spring AI Chat 응답 성공 테스트, Messi 정보 반환 기대")
    void testIsOkChatResponse() {
        // given
        String query = "who is messi?";

        // when
        String response = chatService.getChatResponse(query);

        // then
        assertNotNull(response, "응답 메세지 NULL");
        assertFalse(response.isEmpty(), "응답은 Empty가 아니어야 함.");

        // 출력
        System.out.println("Real response from OpenAI: " + response);
    }

    @Test
    @DisplayName("반환타입 ChatResonse인 경우")
    void testChatResponseReturnType() {
        // given
        String query = "who is messi?";
        ChatClient chatClient = chatClientBuilder.build();
        // when
        ChatResponse chatResponse = chatClient.prompt()
                .user(query)
                .call()
                .chatResponse();
        // then
        System.out.println(chatResponse.toString());
    }

    @Test
    @DisplayName("반환타입이 Entity인 경우")
    void testEntityReturnType() {
        // given
        String query = "generate messi's previous all team";
        ChatClient chatClient = chatClientBuilder.build();
        // when
        Player entity = chatClient.prompt()
                .user(query)
                .call()
                .entity(Player.class);
        // then
        System.out.println(entity.toString());
    }

    @Test
    @DisplayName("반환타입이 List<Entity>인 경우")
    void testListEntityReturnType() {
        // given
        String query = "generate random famous soccer player's previous all team";
        ChatClient chatClient = chatClientBuilder.build();
        // when
        List<Player> players = chatClient.prompt()
                .user(query)
                .call()
                .entity(new ParameterizedTypeReference<List<Player>>() {
                });
        // then
        for (Player player : players) {
            System.out.println(player.toString());
        }
    }

    static class Player{
        String name;
        List<String> teams = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Player name : " + name + "\n");
            sb.append("Teams : " + "\n");
            for (String team : teams) {
                sb.append(team + "\n");
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Player player = (Player) o;
            return Objects.equals(name, player.name) && Objects.equals(teams, player.teams);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, teams);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTeams() {
            return teams;
        }

        public void setTeams(List<String> teams) {
            this.teams = teams;
        }
    }
}
package org.example.demo.ai;

import org.example.demo.SpringBootTestWithProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTestWithProfile("test")
public class SimpleChatModelTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleChatModelTest.class);
    @Autowired
    private ChatModel chatModel;
    
    @Test
    @DisplayName("chat ai 정상 작동 테스트")
    void testBasicChatResponse() {
        // Simple prompt to test chat model
        String prompt = "What is Spring AI?";
        
        // Get response from model
        ChatResponse response = chatModel.call(
            new Prompt(prompt)
        );

        // Assert response is not empty
        assertNotNull(response);
        assertFalse(response.getResult().getOutput().getText().isEmpty());
    }

    @Test
    @DisplayName("Chat ai Prompt 테스트")
    void testPromptTemplate() {
        // Create a template with variables
        PromptTemplate template = new PromptTemplate(
                "Explain {concept} in simple terms."
        );

        // Create prompt with variables
        Prompt prompt = template.create(Map.of("concept", "Spring AI"));

        // Get response
        ChatResponse response = chatModel.call(prompt);

        // Verify response
        assertNotNull(response);
        assertTrue(response.getResult().getOutput().getText().contains("Spring AI"));
    }

    @Test
    @DisplayName("Chat AI Logging Test")
    void testChatModelWithLogging() {
        // Create a ChatClient with logging enabled
        ChatResponse response = ChatClient.builder(chatModel)
                .build().prompt()
                .advisors(new SimpleLoggerAdvisor())  // Add the logging advisor
                .user("Tell me a joke")
                .call()
                .chatResponse();

        // Verify response
        assertNotNull(response);
        assertFalse(response.getResult().getOutput().getText().isEmpty());
    }

    @Test
    @DisplayName("Chat AI Prompt for Code Explanation Test")
    void testCodeExplanationPrompt() {
        // 프롬프트 템플릿에 {language}와 {code}를 명시적으로 추가
        PromptTemplate template = new PromptTemplate(
                """
                Explain the following {language} code in simple terms, focusing on its purpose and key components:
        
                ```
                {code}
                ```
        
                Provide a moderately detailed explanation, suitable for a junior developer.
                """
        );

        // 설명할 대상 코드
        String sampleCode =
                "@Test\n" +
                        "@DisplayName(\"Chat ai Prompt 테스트\")\n" +
                        "void testPromptTemplate() {\n" +
                        "    PromptTemplate template = new PromptTemplate(\n" +
                        "            \"Explain {concept} in simple terms.\"\n" +
                        "    );\n" +
                        "    Prompt prompt = template.create(Map.of(\"concept\", \"Spring AI\"));\n" +
                        "    ChatResponse response = chatModel.call(prompt);\n" +
                        "    assertNotNull(response);\n" +
                        "    assertTrue(response.getResult().getOutput().getContent().contains(\"Spring AI\"));\n" +
                        "}";

        // 프롬프트 생성 시 변수 전달
        Prompt prompt = template.create(Map.of(
                "language", "java",
                "code", sampleCode
        ));

        // AI 모델 호출 후 응답 받기
        ChatResponse response = chatModel.call(prompt);

        // 로그로 응답 출력
        log.info(response.getResult().getOutput().getText());

        // 응답 검증
        assertNotNull(response);
    }

    @Test
    @DisplayName("코드 설명 및 한국어 번역 테스트")
    void testCodeExplanationWithKoreanTranslation() {
        // 프롬프트 템플릿 생성
        PromptTemplate template = new PromptTemplate(
                """
                다음 Java 코드를 분석하고 두 가지 작업을 수행해주세요:
                
                1. 코드의 알고리즘과 주요 로직을 상세히 설명해주세요.
                2. 코드의 목적과 작동 방식을 한국어로 요약해주세요.
                
                ```
                {code}
                ```
                
                응답 형식:
                ## 코드 분석
                (여기에 코드의 알고리즘과 주요 로직에 대한 상세 설명)
                
                ## 한국어 요약
                (여기에 코드의 목적과 작동 방식에 대한 한국어 요약)
                """
        );

        // 설명할 대상 코드
        String javaCode = "import java.io.BufferedReader;\n" +
                excode +
                "}\n";

        // 프롬프트 생성
        Prompt prompt = template.create(Map.of("code", javaCode));

        // AI 모델 호출
        ChatResponse response = chatModel.call(prompt);

        // 로그로 응답 출력
        log.info(response.getResult().getOutput().getText());

        // 응답 검증
        assertNotNull(response);
        String content = response.getResult().getOutput().getText();

        // 한국어 요약 섹션이 있는지 확인
        assertTrue(content.contains("## 한국어 요약"));
    }


    String excode = "import java.io.IOException;\n" +
            "import java.io.InputStreamReader;\n" +
            "import java.util.*;\n" +
            "import java.util.stream.*;\n" +
            "\n" +
            "\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) throws IOException {\n" +
            "        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n" +
            "\n" +
            "        int T = Integer.valueOf(br.readLine());\n" +
            "        while (T-- > 0) {\n" +
            "            int[] NK = Arrays.stream(br.readLine().split(\" \")).mapToInt(Integer::parseInt).toArray();\n" +
            "            int N = NK[0];\n" +
            "            int K = NK[1];\n" +
            "\n" +
            "            int[] times = new int[N + 1];\n" +
            "            int[] timeInput = Arrays.stream(br.readLine().split(\" \")).mapToInt(Integer::parseInt).toArray();\n" +
            "            for (int i = 0; i < timeInput.length; i++) {\n" +
            "                times[i + 1] = timeInput[i];\n" +
            "            }\n" +
            "\n" +
            "            // 연결\n" +
            "            Map<Integer, List<Integer>> graph = new HashMap<>();\n" +
            "            int[] inDegree = new int[N + 1];\n" +
            "            for (int i = 0; i < K; i++) {\n" +
            "                int[] input = Arrays.stream(br.readLine().split(\" \")).mapToInt(Integer::parseInt).toArray();\n" +
            "                int a = input[0];\n" +
            "                int b = input[1];\n" +
            "\n" +
            "                inDegree[b]++;\n" +
            "                graph.computeIfAbsent(a, x -> new ArrayList<>()).add(b);\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            // 타겟\n" +
            "            int target = Integer.valueOf(br.readLine());\n" +
            "\n" +
            "            Deque<Integer> dq = new ArrayDeque<>();\n" +
            "            int[] dp = new int[N + 1];\n" +
            "\n" +
            "            for (int i = 1; i <= N; i++) {\n" +
            "                if (inDegree[i] == 0) {\n" +
            "                    dq.offerLast(i);\n" +
            "                    dp[i] = times[i];\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            // 위상정렬\n" +
            "            while(!dq.isEmpty()) {\n" +
            "                int size = dq.size();\n" +
            "                for(int i = 0 ; i < size ; i++) {\n" +
            "                    int cur = dq.pollFirst();\n" +
            "                    if(graph.containsKey(cur)) {\n" +
            "                        for(int next : graph.get(cur)) {\n" +
            "                            inDegree[next]--;\n" +
            "                            dp[next] = Math.max(dp[next], dp[cur] + times[next]);\n" +
            "                            if(inDegree[next] == 0) {\n" +
            "                                dq.offer(next);\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            System.out.println(dp[target]);\n" +
            "        }\n" +
            "    }\n" +
            "}";

}

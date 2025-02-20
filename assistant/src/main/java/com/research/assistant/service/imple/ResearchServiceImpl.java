package com.research.assistant.service.imple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.research.assistant.model.GeminiResponse;
import com.research.assistant.model.ResearchRequest;
import com.research.assistant.service.ResearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Map;

@Service
public class ResearchServiceImpl implements ResearchService {

    @Value("${gemini.api.url}")
    private String geminiapiurl;

    @Value("${gemini.api.key}")
    private String geminiapikey;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    public ResearchServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }


    @Override
    public String processContent(ResearchRequest request) throws Exception {
        String prompt = buildPrompt(request);
        Map<String, Object> requestBody = Map.of(
        "contents", new Object[] {
            Map.of(
                "parts", new Object[]{
                        Map.of("text", prompt)
                })
        });

        String response = webClient.post()
                .uri(geminiapiurl + geminiapikey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractMessage(response);

    }

    private String extractMessage(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null &&
                        !candidate.getContent().getParts().isEmpty()
                ){
                    return candidate.getContent().getParts().get(0).getText();
                }
            }
        } catch (Exception e){
            return "exception";
        }
        return "";
    }

    private String buildPrompt(ResearchRequest request) throws Exception {
        StringBuilder prompt = new StringBuilder();
        switch (request.getOperation()){
            case "summarize" : prompt.append("Summarize the following statement clearly\n\n");
                                break;
            case "suggest" : prompt.append("based on the following content suggest related topics \n\n");
                                break;
            default: throw new Exception("no prompt");
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}

package com.elif.aiservice.rag;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmService {
    @Value("${openai.api.key}")
    private String apiKey;

    private OpenAiService service;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            service = new OpenAiService(apiKey);
        }
    }

    public String ask(String prompt) {
        if (service == null) {
            return "OPENAI_API_KEY tanimli olmadigi icin LLM cevabi uretilemedi. Soru/prompt alindi, ancak gercek cevap icin ortam degiskeni ayarlanmalidir.";
        }

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(500)
                .build();

        ChatCompletionResult result = service.createChatCompletion(request);
        return result.getChoices().get(0).getMessage().getContent();
    }
}

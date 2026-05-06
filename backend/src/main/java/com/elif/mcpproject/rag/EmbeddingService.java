package com.elif.mcpproject.rag;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    @Value("${openai.api.key}")
    private String apiKey;

    private OpenAiService service;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            service = new OpenAiService(apiKey);
        }
    }

    public List<Double> createEmbedding(String text) {
        if (service == null) {
            return createLocalEmbedding(text);
        }

        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input(List.of(text))
                .build();

        EmbeddingResult result = service.createEmbeddings(request);

        return result.getData().get(0).getEmbedding();
    }

    private List<Double> createLocalEmbedding(String text) {
        int dimensions = 64;
        double[] vector = new double[dimensions];
        String safeText = text == null ? "" : text.toLowerCase(Locale.ROOT);

        for (String token : safeText.split("[^\\p{L}\\p{Nd}]+")) {
            if (token.isBlank()) {
                continue;
            }

            int index = Math.floorMod(token.hashCode(), dimensions);
            vector[index] += 1.0;
        }

        List<Double> out = new ArrayList<>(dimensions);
        for (double value : vector) {
            out.add(value);
        }
        return out;
    }
}

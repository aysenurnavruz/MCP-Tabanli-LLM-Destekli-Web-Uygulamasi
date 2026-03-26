package com.elif.mcpproject.rag;

import com.elif.mcpproject.document.text.DocumentChunk;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MockAssistantProvider {
    public String answer(String userQuestion, List<DocumentChunk> retrieved){
        if (retrieved == null || retrieved.isEmpty()){
            return "(Demo/Mock) Dökümanda ilgili bir parça bulamadım. " +
                    "RAG pipeline hazır; embedding + vector search eklendiğinde daha doğru retrieval yapılacak.\n\n" +
                    "Soru: " + userQuestion;

        }

        String sources = retrieved.stream()
                .map(c-> "#" + c.getChunkIndex())
                .collect(Collectors.joining(","));

        String snippet = retrieved.get(0).getContent();
        snippet = snippet.length() > 400 ? snippet.substring(0,400) + "..." : snippet;

        return "(Demo/Mock RAG)\n"+
                "Soru: " + userQuestion + "\n\n" +
                "Örnek ilgili içerik:\n" + snippet + "\n\n" +
                "Cevap (mock): Dökümanda geçen ilgili kısımlara göre, bu konu temelde yukarıdaki bölümle ilişkili. "+
                "LLM entegrasyonu eklendiğinde bu içerik gerçek yanıt üretimine context olarak verilecek.";
    }
}


package ma.ensa.ebankingver1.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WitAIClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiUrl = "https://api.wit.ai/message?v=20250601&q=";

    @Value("${wit.ai.token}")
    private String witAIToken;

    public WitAIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WitAIResponse processRequest(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + witAIToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            String encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8");
            String fullUrl = apiUrl + encodedPrompt;
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), WitAIResponse.class);
            } else {
                throw new RuntimeException("Erreur de l'API Wit.ai : statut " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'appel à l'API Wit.ai : " + e.getMessage(), e);
        }
    }

    public boolean testModelAvailability() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + witAIToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String testPrompt = "Test model availability";

        try {
            String encodedPrompt = java.net.URLEncoder.encode(testPrompt, "UTF-8");
            String fullUrl = apiUrl + encodedPrompt;
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Erreur lors du test de Wit.ai : " + e.getMessage());
            return false;
        }
    }
}
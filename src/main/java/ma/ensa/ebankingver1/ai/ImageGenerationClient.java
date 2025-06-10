package ma.ensa.ebankingver1.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ImageGenerationClient {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-2-1";

    @Value("${huggingface.api.key}")
    private String apiKey;

    public ImageGenerationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] generateAvatar(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        String requestBody = "{\"inputs\": \"" + prompt + "\", \"parameters\": {\"num_inference_steps\": 50, \"guidance_scale\": 7.5}}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de l'avatar : " + e.getMessage());
        }
    }
}
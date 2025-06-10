package ma.ensa.ebankingver1.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class LibreTranslateClient {
    private static final Logger logger = LoggerFactory.getLogger(LibreTranslateClient.class);
    private final RestTemplate restTemplate;
    private final String apiUrl = "https://translate.argosopentech.com/translate";

    public LibreTranslateClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public String translate(String text, String sourceLang, String targetLang) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestJson = String.format("{\"q\":\"%s\",\"source\":\"%s\",\"target\":\"%s\",\"format\":\"text\"}",
                    text.replace("\"", "\\\""), sourceLang, targetLang);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            logger.info("Sending translation request: {}", requestJson);
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            if (response != null && response.contains("translatedText")) {
                String translated = response.split("\"translatedText\":\"")[1].split("\"")[0];
                logger.info("Translation result: {}", translated);
                return translated;
            }
            logger.warn("Invalid response from LibreTranslate, returning original text");
            return text;
        } catch (Exception e) {
            logger.error("Translation error for text '{}': {}", text, e.getMessage());
            return text;
        }
    }
}

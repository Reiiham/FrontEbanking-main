package ma.ensa.ebankingver1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:mail.properties")
public class Infobip2FAConfig {

    @Value("${infobip.api.key}")
    private String apiKey;

    @Value("${infobip.base.url}")
    private String baseUrl;

    @Value("${infobip.sender.id}")
    private String senderId;

    @Value("${infobip.application.id}")
    private String applicationId;

    @Value("${infobip.message.id}")
    private String messageId;

    // Getters
    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getSenderId() { return senderId; }
    public String getApplicationId() { return applicationId; }
    public String getMessageId() { return messageId; }
}



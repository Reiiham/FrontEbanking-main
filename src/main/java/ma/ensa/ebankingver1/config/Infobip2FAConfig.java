package ma.ensa.ebankingver1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
<<<<<<< HEAD
@PropertySource("classpath:sms.properties")
=======
@PropertySource("classpath:mail.properties")
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
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



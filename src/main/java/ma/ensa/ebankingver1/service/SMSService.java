package ma.ensa.ebankingver1.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        logger.info("Twilio client initialized with Account SID: {}", accountSid);
    }

    public void sendSMS(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();
            logger.info("SMS envoyé avec succès à {} - SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du SMS à {}: {}", toPhoneNumber, e.getMessage());
            throw new RuntimeException("Échec de l'envoi du SMS: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Twilio client...");
        // Twilio does not provide a direct shutdown method, but we log the shutdown for clarity
    }
}
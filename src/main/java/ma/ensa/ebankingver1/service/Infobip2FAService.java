package ma.ensa.ebankingver1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.ensa.ebankingver1.config.Infobip2FAConfig;
import ma.ensa.ebankingver1.model.User2FASession;
import ma.ensa.ebankingver1.repository.User2FASessionRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class Infobip2FAService {
    private static final Logger logger = LoggerFactory.getLogger(Infobip2FAService.class);

    @Autowired
    private Infobip2FAConfig config;

    @Autowired
    private User2FASessionRepository sessionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Infobip2FAService() {
        this.httpClient = new OkHttpClient.Builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean sendPin(String username) {
        try {
            // Get user phone number
            String phoneNumber = userService.getUserPhoneByUsername(username);
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                logger.error("Phone number not found for username: {}", username);
                return false;
            }

            // Gestion manuelle des transactions
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

            try {
                // Delete any existing session for this user
                sessionRepository.deleteByUsername(username);

                // Commit la suppression
                transactionManager.commit(status);
            } catch (Exception e) {
                transactionManager.rollback(status);
                throw e;
            }

            // Send PIN via Infobip 2FA API (en dehors de la transaction)
            String pinId = sendPinViaInfobip(phoneNumber);

            if (pinId != null) {
                // Nouvelle transaction pour sauvegarder
                TransactionStatus saveStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

                try {
                    // Save session with pin ID
                    User2FASession session = new User2FASession(username, phoneNumber, pinId);
                    sessionRepository.save(session);

                    // Commit la sauvegarde
                    transactionManager.commit(saveStatus);

                    logger.info("2FA PIN sent successfully to username: {}, pinId: {}", username, pinId);
                    return true;
                } catch (Exception e) {
                    transactionManager.rollback(saveStatus);
                    throw e;
                }
            } else {
                logger.error("Failed to send 2FA PIN for username: {}", username);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error sending 2FA PIN for username: {}, error: {}", username, e.getMessage());
            return false;
        }
    }

    // Méthode sans transaction pour les appels HTTP
    private String sendPinViaInfobip(String phoneNumber) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("applicationId", config.getApplicationId());
            requestBody.put("messageId", config.getMessageId());
            requestBody.put("from", config.getSenderId());
            requestBody.put("to", phoneNumber);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonBody
            );

            Request request = new Request.Builder()
                    .url(config.getBaseUrl() + "/2fa/2/pin")
                    .method("POST", body)
                    .addHeader("Authorization", "App " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            Response response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                String pinId = jsonResponse.get("pinId").asText();
                logger.info("PIN sent successfully. PIN ID: {}", pinId);
                return pinId;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                logger.error("Failed to send PIN. Status: {}, Body: {}", response.code(), errorBody);
                return null;
            }

        } catch (IOException e) {
            logger.error("IO error sending PIN via Infobip: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error sending PIN via Infobip: {}", e.getMessage());
            return null;
        }
    }

    public boolean verifyPin(String username, String pin) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            User2FASession session = sessionRepository.findByUsernameAndVerifiedFalse(username);

            if (session == null) {
                logger.warn("No active 2FA session found for username: {}", username);
                transactionManager.rollback(status);
                return false;
            }

            // Verify PIN via Infobip 2FA API (en dehors de la transaction DB)
            boolean isValidPin = verifyPinViaInfobip(session.getPinId(), pin);

            if (isValidPin) {
                // Mark session as verified
                sessionRepository.markAsVerified(username);
                transactionManager.commit(status);

                logger.info("2FA PIN verified successfully for username: {}", username);
                return true;
            } else {
                transactionManager.rollback(status);
                logger.warn("Invalid 2FA PIN provided for username: {}", username);
                return false;
            }

        } catch (Exception e) {
            transactionManager.rollback(status);
            logger.error("Error verifying 2FA PIN for username: {}, error: {}", username, e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean verifyPinViaInfobip(String pinId, String pin) {
        try {
            // Prepare request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("pin", pin);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonBody
            );

            Request request = new Request.Builder()
                    .url(config.getBaseUrl() + "/2fa/2/pin/" + pinId + "/verify")
                    .method("POST", body)
                    .addHeader("Authorization", "App " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            Response response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                boolean verified = jsonResponse.get("verified").asBoolean();
                logger.info("PIN verification response: {}", verified);
                return verified;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                logger.error("Failed to verify PIN. Status: {}, Body: {}", response.code(), errorBody);
                return false;
            }

        } catch (IOException e) {
            logger.error("IO error verifying PIN via Infobip: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error verifying PIN via Infobip: {}", e.getMessage());
            return false;
        }
    }
}


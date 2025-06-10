package ma.ensa.ebankingver1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class OperatorApiService {

    private static final Logger logger = LoggerFactory.getLogger(OperatorApiService.class);

    // URLs Reloadly SANDBOX (gratuit)
    private static final String SANDBOX_AUTH_URL = "https://auth.reloadly.com/oauth/token";
    private static final String SANDBOX_TOPUP_URL = "https://topups-sandbox.reloadly.com";

    @Value("${reloadly.client.id}")
    private String clientId;

    @Value("${reloadly.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken;
    private long tokenExpiryTime = 0;

    public boolean processRecharge(String operatorCode, String phoneNumber, BigDecimal amount) {
        try {
            logger.info("Processing recharge for operator: {}, phone: {}, amount: {}",
                    operatorCode, phoneNumber, amount);

            // Tous les opérateurs utilisent Reloadly en sandbox
            return processReloadlyRecharge(phoneNumber, amount, operatorCode);

        } catch (Exception e) {
            logger.error("Error processing recharge: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean processReloadlyRecharge(String phoneNumber, BigDecimal amount, String operatorCode) {
        try {
            // Obtenir un token valide
            String token = getValidAccessToken();

            // D'abord, obtenir l'ID de l'opérateur pour le Maroc
            Integer operatorId = getOperatorId(token, phoneNumber, operatorCode);
            if (operatorId == null) {
                logger.error("Could not find operator ID for: {}", operatorCode);
                return false;
            }

            // Préparer la requête de recharge
            Map<String, Object> recipientPhone = new HashMap<>();
            recipientPhone.put("countryCode", "MA");
            recipientPhone.put("number", phoneNumber);

            Map<String, Object> request = new HashMap<>();
            request.put("recipientPhone", recipientPhone);
            request.put("amount", amount);
            request.put("operatorId", operatorId);
            // Pour les tests, utilisez useLocalAmount: true
            request.put("useLocalAmount", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            logger.info("Sending recharge request to Reloadly sandbox");
            logger.debug("Request payload: {}", request);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    SANDBOX_TOPUP_URL + "/topups", entity, Map.class
            );

            logger.info("Reloadly response status: {}", response.getStatusCode());
            logger.info("Reloadly response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                boolean success = "SUCCESSFUL".equals(status) || "PENDING".equals(status);
                logger.info("Recharge result: {}, status: {}", success, status);
                return success;
            }

            return false;

        } catch (Exception e) {
            logger.error("Error in Reloadly recharge: {}", e.getMessage(), e);
            return false;
        }
    }

    private Integer getOperatorId(String token, String phoneNumber, String operatorCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Obtenir tous les opérateurs du Maroc
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    SANDBOX_TOPUP_URL + "/operators/countries/MA",
                    HttpMethod.GET,
                    entity,
                    Map[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map[] operators = response.getBody();

                for (Map<String, Object> operator : operators) {
                    String name = (String) operator.get("name");
                    logger.info("Available operator: {}", name);

                    // Mapping des noms d'opérateurs
                    if (isOperatorMatch(name, operatorCode)) {
                        Integer operatorId = (Integer) operator.get("operatorId");
                        logger.info("Found operator ID {} for {}", operatorId, operatorCode);
                        return operatorId;
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error getting operator ID: {}", e.getMessage());
        }

        return null;
    }

    private boolean isOperatorMatch(String operatorName, String operatorCode) {
        String name = operatorName.toLowerCase();
        String code = operatorCode.toLowerCase();

        return (code.equals("iam") && name.contains("maroc telecom")) ||
                (code.equals("orange") && name.contains("orange")) ||
                (code.equals("inwi") && name.contains("inwi"));
    }

    private String getValidAccessToken() {
        // Vérifier si le token est encore valide (avec marge de 5 minutes)
        if (accessToken != null && System.currentTimeMillis() < (tokenExpiryTime - 300000)) {
            return accessToken;
        }

        // Obtenir un nouveau token
        return refreshAccessToken();
    }

    private String refreshAccessToken() {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("client_id", clientId);
            request.put("client_secret", clientSecret);
            request.put("grant_type", "client_credentials");
            request.put("audience", "https://topups-sandbox.reloadly.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            logger.info("Refreshing access token from Reloadly");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    SANDBOX_AUTH_URL, entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String newToken = (String) body.get("access_token");
                Integer expiresIn = (Integer) body.get("expires_in");

                if (newToken != null) {
                    this.accessToken = newToken;
                    this.tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L);
                    logger.info("Successfully refreshed access token, expires in {} seconds", expiresIn);
                    return newToken;
                }
            }

            throw new RuntimeException("Failed to get access token from response");

        } catch (Exception e) {
            logger.error("Error refreshing access token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    // Méthode utilitaire pour tester la connectivité
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = getValidAccessToken();
            result.put("status", "SUCCESS");
            result.put("message", "Successfully connected to Reloadly sandbox");
            result.put("hasToken", token != null);

            // Tester en obtenant la liste des opérateurs
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    SANDBOX_TOPUP_URL + "/operators/countries/MA",
                    HttpMethod.GET,
                    entity,
                    Map[].class
            );

            result.put("operatorsCount", response.getBody() != null ? response.getBody().length : 0);

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }
}
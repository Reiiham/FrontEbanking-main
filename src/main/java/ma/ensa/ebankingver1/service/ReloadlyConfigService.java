package ma.ensa.ebankingver1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class ReloadlyConfigService {

    @Value("${reloadly.client.id:}")
    private String clientId;

    @Value("${reloadly.client.secret:}")
    private String clientSecret;

    @Value("${reloadly.sandbox.mode:true}")
    private boolean sandboxMode;

    // URLs Reloadly
    private static final String SANDBOX_BASE_URL = "https://topups-sandbox.reloadly.com";
    private static final String PRODUCTION_BASE_URL = "https://topups.reloadly.com";
    private static final String AUTH_URL = "https://auth.reloadly.com/oauth/token";

    public boolean isConfigured() {
        return clientId != null && !clientId.trim().isEmpty() &&
                clientSecret != null && !clientSecret.trim().isEmpty();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public boolean isSandboxMode() {
        return sandboxMode;
    }

    public String getBaseUrl() {
        return sandboxMode ? SANDBOX_BASE_URL : PRODUCTION_BASE_URL;
    }

    public String getAuthUrl() {
        return AUTH_URL;
    }

    public String getAudience() {
        return sandboxMode ? "https://topups-sandbox.reloadly.com" : "https://topups.reloadly.com";
    }

    public Map<String, Object> getConfigurationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", isConfigured());
        status.put("sandboxMode", sandboxMode);
        status.put("baseUrl", getBaseUrl());
        status.put("hasClientId", clientId != null && !clientId.trim().isEmpty());
        status.put("hasClientSecret", clientSecret != null && !clientSecret.trim().isEmpty());
        return status;
    }

    // Limites par défaut pour le sandbox
    public Map<String, Integer> getDefaultLimits() {
        Map<String, Integer> limits = new HashMap<>();
        if (sandboxMode) {
            limits.put("minAmount", 1);
            limits.put("maxAmount", 500);
            limits.put("dailyLimit", 1000);
        } else {
            limits.put("minAmount", 5);
            limits.put("maxAmount", 1000);
            limits.put("dailyLimit", 5000);
        }
        return limits;
    }
}
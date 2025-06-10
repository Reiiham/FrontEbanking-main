package ma.ensa.ebankingver1.DTO;

import java.util.List;

public class ActivateServicesRequest {
    private String clientId;
    private List<String> services;

    // Getters et Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
}

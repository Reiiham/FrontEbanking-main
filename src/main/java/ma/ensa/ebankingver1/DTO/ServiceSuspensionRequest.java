package ma.ensa.ebankingver1.DTO;


import ma.ensa.ebankingver1.model.BankService;

import java.util.List;

public class ServiceSuspensionRequest {
    private Long clientId;
    private List<BankService> servicesToSuspend;
    private String reason;
    private String notificationMessage;

    // Getters et Setters
    public Long getClientId() {
        return clientId;
    }
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    public List<BankService> getServicesToSuspend() {
        return servicesToSuspend;
    }
    public void setServicesToSuspend(List<BankService> servicesToSuspend) {
        this.servicesToSuspend = servicesToSuspend;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getNotificationMessage() {
        return notificationMessage;
    }
    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}

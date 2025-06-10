package ma.ensa.ebankingver1.DTO;

public class ClientDeletionRequest {
    private Long clientId;

    public ClientDeletionRequest() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
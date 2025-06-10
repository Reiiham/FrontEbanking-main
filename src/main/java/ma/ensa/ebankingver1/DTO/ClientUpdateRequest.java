package ma.ensa.ebankingver1.DTO;

public class ClientUpdateRequest extends SecureActionRequest{
    private Long clientId;
    private String newFirstName;
    private String newLastName;
    private String newEmail;
    private String newTel;
    private String supervisorPassword;

    public ClientUpdateRequest() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getNewFirstName() {
        return newFirstName;
    }

    public void setNewFirstName(String newFirstName) {
        this.newFirstName = newFirstName;
    }

    public String getNewLastName() {
        return newLastName;
    }

    public void setNewLastName(String newLastName) {
        this.newLastName = newLastName;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewTel() {
        return newTel;
    }

    public void setNewTel(String newTel) {
        this.newTel = newTel;
    }

    public String getSupervisorPassword() {
        return supervisorPassword;
    }

    public void setSupervisorPassword(String supervisorPassword) {
        this.supervisorPassword = supervisorPassword;
    }
}

package ma.ensa.ebankingver1.DTO;

import ma.ensa.ebankingver1.model.User;

public class ClientBasicDTO {
    private Long clientId;
    private String fullName;

    // getters & setters
    public Long getClientId() {
        return clientId;
    }
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public static ClientBasicDTO fromUser(User user) {
        ClientBasicDTO dto = new ClientBasicDTO();
        dto.setClientId(user.getId());
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        return dto;
    }
}
package ma.ensa.ebankingver1.DTO;

import jakarta.validation.constraints.*;

public class SetPasswordRequest {
    private String token;

    private String newPassword;

    // Getters et setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

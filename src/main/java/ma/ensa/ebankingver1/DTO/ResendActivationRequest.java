package ma.ensa.ebankingver1.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResendActivationRequest {
    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    private String email;

    // Getters et setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

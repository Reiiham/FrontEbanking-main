package ma.ensa.ebankingver1.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PhoneRechargeRequest {

    @NotBlank(message = "Le code opérateur est requis")
    private String operatorCode;

    @NotBlank(message = "Le numéro de téléphone est requis")
    @Pattern(regexp = "^(\\+212|0)[567]\\d{8}$",
            message = "Format de numéro de téléphone invalide. Utilisez le format marocain.")
    private String phoneNumber;

    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "1.0", message = "Le montant minimum est de 1 DH")
    @DecimalMax(value = "500.0", message = "Le montant maximum est de 500 DH")
    private BigDecimal amount;

    @NotBlank(message = "Le PIN de transaction est requis")
    @Size(min = 4, max = 6, message = "Le PIN doit contenir entre 4 et 6 chiffres")
    @Pattern(regexp = "\\d+", message = "Le PIN ne doit contenir que des chiffres")
    private String transactionPin;

    // Constructeurs
    public PhoneRechargeRequest() {}

    public PhoneRechargeRequest(String operatorCode, String phoneNumber,
                                BigDecimal amount, String transactionPin) {
        this.operatorCode = operatorCode;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.transactionPin = transactionPin;
    }

    // Getters et Setters
    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionPin() {
        return transactionPin;
    }

    public void setTransactionPin(String transactionPin) {
        this.transactionPin = transactionPin;
    }

    @Override
    public String toString() {
        return "PhoneRechargeRequest{" +
                "operatorCode='" + operatorCode + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", amount=" + amount +
                ", transactionPin='****'" + // Ne pas exposer le PIN
                '}';
    }
}
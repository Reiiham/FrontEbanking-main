// 3. DTOs pour les requêtes et réponses
package ma.ensa.ebankingver1.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PhoneRechargeResponse {
    private String transactionReference;
    private String status;
    private String message;
    private BigDecimal amount;
    private String phoneNumber;
    private LocalDateTime timestamp;

    // Constructeur pour succès
    public PhoneRechargeResponse(String transactionReference, BigDecimal amount,
                                 String phoneNumber) {
        this.transactionReference = transactionReference;
        this.status = "SUCCESS";
        this.message = "Recharge effectuée avec succès";
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public PhoneRechargeResponse() {
    }
    // Constructeur par défaut
    public PhoneRechargeResponse(String transactionReference, String status, String message,
                                 BigDecimal amount, String phoneNumber, LocalDateTime timestamp) {
        this.transactionReference = transactionReference;
        this.status = status;
        this.message = message;
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.timestamp = timestamp;
    }

    // Constructeur pour échec
    public PhoneRechargeResponse(String message) {
        this.status = "FAILED";
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}

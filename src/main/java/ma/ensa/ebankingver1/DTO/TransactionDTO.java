package ma.ensa.ebankingver1.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionDTO {
    private String transactionId;
    private double amount;
    private LocalDateTime date;

    // constructeur, getters/setters
    public TransactionDTO() {
        super();
    }
    public TransactionDTO(Long transactionId, double amount, LocalDate date) {
        super();
    }

    public TransactionDTO(String id, double amount, LocalDateTime date) {
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}

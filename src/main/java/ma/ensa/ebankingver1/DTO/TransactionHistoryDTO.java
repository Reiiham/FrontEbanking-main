package ma.ensa.ebankingver1.DTO;

import ma.ensa.ebankingver1.model.Transaction;

import java.time.LocalDateTime;

public class TransactionHistoryDTO {
    private String id;
    private String type;
    private Double amount;
    private LocalDateTime date;
    private String accountNumber;
    private Double balance;

    // Constructeurs
    public TransactionHistoryDTO() {}

    public TransactionHistoryDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.date = transaction.getDate();
        this.accountNumber = transaction.getAccount() != null ? transaction.getAccount().getRib() : null;
        this.balance = transaction.getAccount() != null ? transaction.getAccount().getBalance() : null;
    }

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}

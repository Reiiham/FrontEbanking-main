package ma.ensa.ebankingver1.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String id; // UUID
    private String type; // deposit, withdraw, transfer, etc.
    private double amount;
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BankAccount account;

    @ManyToOne
    @JoinColumn(name = "clientId")
    private User user;


    // getters and setters
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public double getAmount() {
        return amount;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setAccount(BankAccount account) {
        this.account = account;
    }
    public BankAccount getAccount() {
        return account;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }
}
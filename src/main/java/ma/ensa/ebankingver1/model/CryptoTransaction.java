package ma.ensa.ebankingver1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
@Table(name = "crypto_transactions")
public class CryptoTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currency;
    private double amount;
    private String type; // BUY or SELL
    private double price;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private CryptoWallet wallet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public CryptoWallet getWallet() {
        return wallet;
    }
    public void setWallet(CryptoWallet wallet) {
        this.wallet = wallet;
    }
    @Override
    public String toString() {
        return "CryptoTransaction{" +
                "id=" + id +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", timestamp=" + timestamp +
                ", wallet=" + wallet.getId() +
                '}';
    }
}

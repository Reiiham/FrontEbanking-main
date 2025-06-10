package ma.ensa.ebankingver1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    private String rib;
    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BankAccount account;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BankAccount getAccount() { return account; }
    public void setAccount(BankAccount account) { this.account = account; }

    @Override
    public String toString() {
        return "Beneficiary{" +
                "id='" + id + '\'' +
                ", client=" + client +
                ", rib='" + rib + '\'' +
                ", name='" + name + '\'' +
                ", account=" + account +
                '}';
    }
}

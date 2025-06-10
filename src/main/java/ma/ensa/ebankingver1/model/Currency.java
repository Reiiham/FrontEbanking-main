package ma.ensa.ebankingver1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code_iso", nullable = false, unique = true)
    private String codeISO;

    @Column(name = "exchange_rate", nullable = false)
    private double exchangeRate;
    @Column(name = "isDefault", nullable = false)
    private boolean isDefault;

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeISO() {
        return codeISO;
    }

    public void setCodeISO(String codeISO) {
        this.codeISO = codeISO;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", codeISO='" + codeISO + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", isDefault=" + isDefault +
                '}';
    }

}
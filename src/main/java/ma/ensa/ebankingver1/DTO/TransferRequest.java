package ma.ensa.ebankingver1.DTO;

public class TransferRequest {
    private String fromAccountId;
    private String toRib;
    private double amount;
    private String description;
    private String beneficiaryName;

    // Getters et setters
    public String getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }
    public String getToRib() { return toRib; }
    public void setToRib(String toRib) { this.toRib = toRib; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
}

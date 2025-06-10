package ma.ensa.ebankingver1.DTO;

public class BeneficiaryDTO {
    private String id;
    private String rib;
    private String name;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
public  String getAccountId() {
        return id;
    }
    public void setAccountId(String id) {
        this.id = id;

    }
}
package ma.ensa.ebankingver1.DTO;

public class EnrollmentRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String tel;
    private String birthDate; // format: yyyy-MM-dd
    private String accountType;
    private double balance;
    private String cin;

    public String getCin() {
        return cin;
    }
    public void setCin(String cin) {
        this.cin = cin;
    }


    public EnrollmentRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

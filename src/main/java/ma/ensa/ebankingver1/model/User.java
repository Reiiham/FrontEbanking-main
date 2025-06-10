package ma.ensa.ebankingver1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD
    @Column(name = "username", unique = true, nullable = false)
    private String username;


    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "Tel")
=======

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "Tel", length = 20)
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    private String Tel;

    @Column(name = "Birth_Date")
    private LocalDate Birth_Date;

    @Enumerated(EnumType.STRING)
<<<<<<< HEAD
    @Column(name = "role")
    private Role role;
    @Column(name = "password")
    private String password;


    @Column(name = "must_change_password")
    private Boolean mustChangePassword;
    @Column(unique = true, nullable = false)
    private String cin;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Beneficiary> beneficiaries = new ArrayList<>();

    public List<Beneficiary> getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(List<Beneficiary> beneficiaries) { this.beneficiaries = beneficiaries; }

    // Getters et Setters
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    @Column(name = "compte_bloque")
    private Boolean compteBloque;
=======
    @Column(name = "role", length = 20)
    private Role role;

    @Column(name = "password", length = 255) // BCrypt fait ~60 caractères
    private String password;

    @Column(name = "must_change_password")
    private Boolean mustChangePassword;

    @Column(unique = true, nullable = false, length = 20)
    private String cin;

    @Column(name = "compte_bloque")
    private Boolean compteBloque;

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    @Column(name = "documents_complets")
    private Boolean documentsComplets;

    @ElementCollection(fetch = FetchType.EAGER)
<<<<<<< HEAD
    private List<String> servicesActifs = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<BankAccount> accounts;

    public boolean isCompteBloque() { return compteBloque; }
    public boolean isDocumentsComplets() { return documentsComplets; }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SuspendedService> suspendedServices = new ArrayList<>();




/*
    @OneToMany(mappedBy = "user")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BankAccount> bankAccounts;


 */
    @Column(name = "token")
=======
    @CollectionTable(name = "user_services_actifs", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "service", length = 100)
    private List<String> servicesActifs = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) // Changé en LAZY pour performance
    private List<BankAccount> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SuspendedService> suspendedServices = new ArrayList<>();

    // IMPORTANT: Token peut être très long (JWT), utiliser TEXT
    @Column(name = "token", columnDefinition = "TEXT")
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    private String token;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

<<<<<<< HEAD
    // Getters et setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }


    public User() {
    }
    public List<String> getServicesActifs() { return servicesActifs; }
    public void setServicesActifs(List<String> servicesActifs) { this.servicesActifs = servicesActifs; }

    public List<BankAccount> getAccounts() { return accounts; }
    public void setAccounts(List<BankAccount> accounts) { this.accounts = accounts; }

    public Boolean getCompteBloque() { return compteBloque; }
    public void setCompteBloque(boolean compteBloque) {
        this.compteBloque = compteBloque;
    }

    public Boolean getDocumentsComplets() { return documentsComplets; }
    public void setDocumentsComplets(boolean documentsComplets) {
        this.documentsComplets = documentsComplets;
    }

    public List<SuspendedService> getSuspendedServices() {
        return suspendedServices;
    }

    public void setSuspendedServices(List<SuspendedService> suspendedServices) {
        this.suspendedServices = suspendedServices;
    }


=======
    // Constructeurs
    public User() {
    }

    // Getters et Setters
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

<<<<<<< HEAD
=======
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
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
<<<<<<< HEAD
=======

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String tel) {
<<<<<<< HEAD
        this.Tel=tel;
=======
        this.Tel = tel;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    }

    public LocalDate getBirth_Date() {
        return Birth_Date;
    }

    public void setBirth_Date(LocalDate birth_Date) {
        this.Birth_Date = birth_Date;
    }

    public Role getRole() {
        return role;
    }
<<<<<<< HEAD
    public void setRole(Role role){
        this.role = role;
    }
=======

    public void setRole(Role role) {
        this.role = role;
    }

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

<<<<<<< HEAD

=======
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
<<<<<<< HEAD
    /*
    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }
    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

     */

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }


=======

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public Boolean getCompteBloque() {
        return compteBloque;
    }

    public void setCompteBloque(Boolean compteBloque) {
        this.compteBloque = compteBloque;
    }

    public Boolean getDocumentsComplets() {
        return documentsComplets;
    }

    public void setDocumentsComplets(Boolean documentsComplets) {
        this.documentsComplets = documentsComplets;
    }

    public boolean isCompteBloque() {
        return compteBloque != null ? compteBloque : false;
    }

    public boolean isDocumentsComplets() {
        return documentsComplets != null ? documentsComplets : false;
    }

    public List<String> getServicesActifs() {
        return servicesActifs;
    }

    public void setServicesActifs(List<String> servicesActifs) {
        this.servicesActifs = servicesActifs;
    }

    public List<BankAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BankAccount> accounts) {
        this.accounts = accounts;
    }

    public List<SuspendedService> getSuspendedServices() {
        return suspendedServices;
    }

    public void setSuspendedServices(List<SuspendedService> suspendedServices) {
        this.suspendedServices = suspendedServices;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", Tel='" + Tel + '\'' +
                ", Birth_Date=" + Birth_Date +
                ", role=" + role +
<<<<<<< HEAD
                ", password='" + password + '\'' +
                ", token='" + token + '\'' +
                ", mustChangePassword=" + mustChangePassword +
                ", cin='" + cin + '\'' +
=======
                ", cin='" + cin + '\'' +
                ", compteBloque=" + compteBloque +
                ", documentsComplets=" + documentsComplets +
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
                '}';
    }
}
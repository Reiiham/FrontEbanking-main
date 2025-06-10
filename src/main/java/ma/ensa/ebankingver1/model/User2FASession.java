package ma.ensa.ebankingver1.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "user_2fa_session")
public class User2FASession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String pinId; // Infobip pin ID

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private boolean verified = false;

    // Constructors
    public User2FASession() {}

    public User2FASession(String username, String phoneNumber, String pinId) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.pinId = pinId;
        this.createdAt = new Date();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPinId() { return pinId; }
    public void setPinId(String pinId) { this.pinId = pinId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}


package ma.ensa.ebankingver1.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "phone_recharges")

public class PhoneRecharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String operatorCode; // IAM, ORANGE, INWI

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String clientAccountNumber;

    @Enumerated(EnumType.STRING)
    private RechargeStatus status;

    @Column(unique = true)
    private String transactionReference;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private String failureReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOperatorCode() {
        return operatorCode;
    }
    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getClientAccountNumber() {
        return clientAccountNumber;
    }
    public void setClientAccountNumber(String clientAccountNumber) {
        this.clientAccountNumber = clientAccountNumber;
    }
    public RechargeStatus getStatus() {
        return status;
    }
    public void setStatus(RechargeStatus status) {
        this.status = status;
    }
    public String getTransactionReference() {
        return transactionReference;
    }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    public String getFailureReason() {
        return failureReason;
    }
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public PhoneRecharge() {
    }
    public PhoneRecharge(String phoneNumber, String operatorCode, BigDecimal amount, String clientAccountNumber) {
        this.phoneNumber = phoneNumber;
        this.operatorCode = operatorCode;
        this.amount = amount;
        this.clientAccountNumber = clientAccountNumber;
        this.status = ma.ensa.ebankingver1.model.RechargeStatus.PENDING; // Default status
    }

    @Override
    public String toString() {
        return "PhoneRecharge{" +
                "id=" + id +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", operatorCode='" + operatorCode + '\'' +
                ", amount=" + amount +
                ", clientAccountNumber='" + clientAccountNumber + '\'' +
                ", status=" + status +
                ", transactionReference='" + transactionReference + '\'' +
                ", createdAt=" + createdAt +
                ", processedAt=" + processedAt +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.transactionReference = generateTransactionReference();
    }

    private String generateTransactionReference() {
        return "REF_" + System.currentTimeMillis() + "_" +
                (int)(Math.random() * 1000);
    }
}

package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.PhoneRecharge;
import ma.ensa.ebankingver1.model.RechargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhoneRechargeRepository extends JpaRepository<PhoneRecharge, Long> {

    List<PhoneRecharge> findByClientAccountNumberOrderByCreatedAtDesc(String accountNumber);

    List<PhoneRecharge> findByStatusAndCreatedAtBefore(RechargeStatus status, LocalDateTime dateTime);

    @Query("SELECT pr FROM PhoneRecharge pr WHERE pr.clientAccountNumber = :accountNumber " +
            "AND pr.createdAt BETWEEN :startDate AND :endDate")
    List<PhoneRecharge> findRechargeHistory(@Param("accountNumber") String accountNumber,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    boolean existsByTransactionReference(String transactionReference);
}

package ma.ensa.ebankingver1.repository;
import ma.ensa.ebankingver1.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long clientId);

    List<Transaction> findByAccountIdOrderByDateDesc(String accountId);

    // Dans TransactionRepository
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND (t.type = 'VIREMENT_SORTANT' OR t.type = 'VIREMENT_ENTRANT') ORDER BY t.date DESC")
    Page<Transaction> findTransfersByUserId(@Param("userId") Long userId, Pageable pageable);
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<Transaction> findByAccountIdAndDateBetween(@Param("accountId") String accountId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

}

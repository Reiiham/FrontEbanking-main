package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.CryptoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Long> {
    List<CryptoTransaction> findByWalletUserIdOrderByTimestampDesc(Long userId);
}

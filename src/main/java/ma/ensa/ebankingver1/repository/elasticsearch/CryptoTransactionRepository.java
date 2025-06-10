package ma.ensa.ebankingver1.repository.elasticsearch;

import ma.ensa.ebankingver1.model.elasticsearch.CryptoTransaction;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CryptoTransactionRepository extends ElasticsearchRepository<CryptoTransaction, String> {

    // Main method for getting user's transaction history
    List<CryptoTransaction> findByUserIdOrderByTimestampDesc(Long userId);

    // Additional useful methods
    List<CryptoTransaction> findByUserIdAndSymbolOrderByTimestampDesc(Long userId, String symbol);

    List<CryptoTransaction> findByUserIdAndSideOrderByTimestampDesc(Long userId, String side);

    List<CryptoTransaction> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<CryptoTransaction> findByUserIdAndSymbolAndTimestampBetweenOrderByTimestampDesc(
            Long userId, String symbol, LocalDateTime start, LocalDateTime end);
}
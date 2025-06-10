package ma.ensa.ebankingver1.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
<<<<<<< HEAD
import co.elastic.clients.elasticsearch.core.IndexResponse;
import ma.ensa.ebankingver1.model.elasticsearch.CryptoTransaction;
import ma.ensa.ebankingver1.repository.elasticsearch.CryptoTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
=======
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import ma.ensa.ebankingver1.model.CryptoTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

@Service
public class CryptoElasticSyncService {

<<<<<<< HEAD
    @Autowired
    private CryptoTransactionRepository repository;

    @Autowired(required = false) // Make optional to prevent startup issues
    private ElasticsearchClient elasticsearchClient;

    /**
     * Sauvegarde une transaction crypto (with error handling)
     */
    public void save(Long userId, String symbol, String side, double quantity, double price) {
        try {
            CryptoTransaction transaction = new CryptoTransaction();
            transaction.setId(userId + "_" + symbol + "_" + LocalDateTime.now());
            transaction.setUserId(userId);
            transaction.setSymbol(symbol);
            transaction.setSide(side);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setTimestamp(LocalDateTime.now());

            // Try to save with repository first (safer)
            CryptoTransaction saved = repository.save(transaction);
            System.out.println("Transaction saved successfully: " + saved.getId());

        } catch (Exception e) {
            // Log the error but don't fail the transaction
            System.err.println("Failed to save to Elasticsearch: " + e.getMessage());
            // You could also save to a backup database here
            saveToBackupStorage(userId, symbol, side, quantity, price);
        }
    }

    /**
     * Alternative save method using direct client (if needed)
     */
    public void saveWithClient(Long userId, String symbol, String side, double quantity, double price) {
        try {
            if (elasticsearchClient == null) {
                System.err.println("Elasticsearch client not available");
                return;
            }

            CryptoTransaction transaction = new CryptoTransaction();
            transaction.setId(userId + "_" + symbol + "_" + LocalDateTime.now());
            transaction.setUserId(userId);
            transaction.setSymbol(symbol);
            transaction.setSide(side);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setTimestamp(LocalDateTime.now());

            IndexResponse response = elasticsearchClient.index(builder -> builder
                    .index("crypto_transactions")
                    .id(transaction.getId().toString())
                    .document(transaction)
            );

            // Handle seqNo() safely
            try {
                Long seqNo = response.seqNo();
                System.out.println("Transaction indexed with seqNo: " + seqNo);
            } catch (NoSuchMethodError e) {
                // seqNo() method not available, use alternative
                System.out.println("Transaction indexed successfully (seqNo not available)");
            }

        } catch (Exception e) {
            System.err.println("Failed to save with client: " + e.getMessage());
            saveToBackupStorage(userId, symbol, side, quantity, price);
        }
    }

    /**
     * Backup storage method (could save to database)
     */
    private void saveToBackupStorage(Long userId, String symbol, String side, double quantity, double price) {
        // Implement backup storage logic here
        System.out.println("Saving to backup: User=" + userId + ", Symbol=" + symbol +
                ", Side=" + side + ", Qty=" + quantity + ", Price=" + price);
    }

    /**
     * Récupère l'historique des transactions
     */
    public List<CryptoTransaction> findHistory(Long userId) {
        try {
            return repository.findByUserIdOrderByTimestampDesc(userId);
        } catch (Exception e) {
            System.err.println("Failed to fetch history: " + e.getMessage());
            return List.of(); // Return empty list instead of failing
        }
    }

    /**
     * Recherche avec filtres (with error handling)
     */
    public List<Map<String, Object>> searchTransactions(Long userId, String currency,
                                                        LocalDateTime start, LocalDateTime end) {
        try {
            List<CryptoTransaction> transactions;

            if (currency != null && start != null && end != null) {
                transactions = repository.findByUserIdAndSymbolAndTimestampBetweenOrderByTimestampDesc(
                        userId, currency, start, end);
            } else if (start != null && end != null) {
                transactions = repository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                        userId, start, end);
            } else {
                transactions = repository.findByUserIdOrderByTimestampDesc(userId);
            }

            // Convert to Map format
            return transactions.stream()
                    .map(this::transactionToMap)
                    .toList();

        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            return List.of(); // Return empty list instead of failing
        }
    }

    private Map<String, Object> transactionToMap(CryptoTransaction transaction) {
        return Map.of(
                "id", transaction.getId(),
                "userId", transaction.getUserId(),
                "symbol", transaction.getSymbol(),
                "side", transaction.getSide(),
                "quantity", transaction.getQuantity(),
                "price", transaction.getPrice(),
                "timestamp", transaction.getTimestamp()
        );
    }
=======
    private static final Logger logger = LoggerFactory.getLogger(CryptoElasticSyncService.class);

    @Autowired
    private ElasticsearchClient client;


    @Async
    public CompletableFuture<Void> index(CryptoTransaction tx) {
        try {
            Map<String, Object> doc = Map.of(
                    "userId", tx.getWallet().getUser().getId(),
                    "currency", tx.getCurrency(),
                    "type", tx.getType(),
                    "amount", tx.getAmount(),
                    "price", tx.getPrice(),
                    "timestamp", tx.getTimestamp().toString()
            );
            client.index(i -> i.index("crypto-transactions").document(doc));
            logger.info("Indexed transaction for userId: {}, currency: {}",
                    tx.getWallet().getUser().getId(), tx.getCurrency());
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            logger.error("Failed to index transaction for userId: {}, currency: {}",
                    tx.getWallet().getUser().getId(), tx.getCurrency(), e);
            return CompletableFuture.failedFuture(e);
        }

    }

    public List<Map<String, Object>> searchTransactions(Long userId, String currency,
                                                        LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Query> queries = new ArrayList<>();
            if (userId != null) {
                queries.add(Query.of(q -> q.match(m -> m.field("userId").query(userId.toString()))));
            }
            if (currency != null && !currency.isEmpty()) {
                queries.add(Query.of(q -> q.match(m -> m.field("currency").query(currency))));
            }
            if (startDate != null && endDate != null) {
                queries.add(Query.of(q -> q
                        .range(r -> r
                                .field("timestamp")
                                .gte(JsonData.fromJson(startDate.toString()))
                                .lte(JsonData.fromJson(endDate.toString()))
                        )
                ));
            }

            SearchResponse<Map> response = client.search(s -> s
                            .index("crypto-transactions")
                            .query(q -> q.bool(b -> b.must(queries)))
                            .size(100), // Limit to 100 results
                    Map.class
            );

            List<Map<String, Object>> results = new ArrayList<>();
            response.hits().hits().forEach(hit -> results.add(hit.source()));
            logger.info("Found {} transactions for userId: {}, currency: {}",
                    results.size(), userId, currency);
            return results;
        } catch (IOException e) {
            logger.error("Failed to search transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
}
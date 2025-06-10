package ma.ensa.ebankingver1.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

@Service
public class CryptoElasticSyncService {

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
}
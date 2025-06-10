package ma.ensa.ebankingver1.controller;

import ma.ensa.ebankingver1.model.CryptoTransaction;
import ma.ensa.ebankingver1.repository.CryptoTransactionRepository;
import ma.ensa.ebankingver1.service.BinanceTestnetClient;
import ma.ensa.ebankingver1.service.CryptoElasticSyncService;
import ma.ensa.ebankingver1.service.CryptoRateService;
import ma.ensa.ebankingver1.service.CryptoTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crypto")
public class CryptoController {

    private final CryptoTradeService tradeService;
    private final CryptoRateService rateService;
    private final CryptoTransactionRepository txRepo;
    private final BinanceTestnetClient binanceTestnetClient;
    private final CryptoElasticSyncService elasticService;

    @Autowired
    public CryptoController(CryptoTradeService tradeService,
                            CryptoRateService rateService,
                            CryptoTransactionRepository txRepo,
                            BinanceTestnetClient binanceTestnetClient
    , CryptoElasticSyncService elasticService) {
        this.tradeService = tradeService;
        this.rateService = rateService;
        this.txRepo = txRepo;
        this.binanceTestnetClient = binanceTestnetClient;
        this.elasticService = elasticService;
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestParam Long userId,
                                      @RequestParam String symbol,
                                      @RequestParam double usd) {
        try {
            if (!symbol.endsWith("USDT")) {
                throw new IllegalArgumentException("Symbol must be a USDT trading pair (e.g., BTCUSDT)");
            }
            String response = tradeService.buyCrypto(userId, symbol, usd);
            return ResponseEntity.ok("Achat réussi: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur achat: " + e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sell(@RequestParam Long userId,
                                       @RequestParam String symbol,
                                       @RequestParam double quantity) {
        try {
            if (!symbol.endsWith("USDT")) {
                throw new IllegalArgumentException("Symbol must be a USDT trading pair (e.g., BTCUSDT)");
            }
            String response = tradeService.sellCrypto(userId, symbol, quantity);
            return ResponseEntity.ok("Vente réussie: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur vente: " + e.getMessage());
        }
    }

    @GetMapping("/rate/{symbol}")
    public ResponseEntity<Double> getRate(@PathVariable String symbol) {
        try {
            if (!symbol.endsWith("USDT")) {
                throw new IllegalArgumentException("Symbol must be a USDT trading pair (e.g., BTCUSDT)");
            }
            double rate = rateService.getRate(symbol);
            return ResponseEntity.ok(rate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @GetMapping("/history/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CryptoTransaction>> history(@PathVariable Long userId) {
        try {
            List<CryptoTransaction> transactions = txRepo.findByWalletUserIdOrderByTimestampDesc(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @GetMapping("/rates")
    public ResponseEntity<Map<String, Double>> getAllRates() {
        try {
            Map<String, Double> rates = rateService.getAllRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/testnet/address")
    public ResponseEntity<String> getTestnetAddress(@RequestParam String currency) {
        try {
            String address = binanceTestnetClient.getDepositAddress(currency);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur: " + e.getMessage());
        }
    }
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Double>> getBalance(
            @RequestParam Long userId,
            @RequestParam(required = false) String currency) throws Exception {
        Map<String, Double> balance = tradeService.getBalance(userId, currency);
        return ResponseEntity.ok(balance);
    }
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchTransactions(
            @RequestParam Long userId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String startDate, // ISO format: yyyy-MM-dd'T'HH:mm:ss
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
            List<Map<String, Object>> transactions = elasticService.searchTransactions(userId, currency, start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}

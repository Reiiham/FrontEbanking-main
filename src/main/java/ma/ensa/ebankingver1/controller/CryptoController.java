package ma.ensa.ebankingver1.controller;

<<<<<<< HEAD
import ma.ensa.ebankingver1.model.elasticsearch.CryptoTransaction;
import ma.ensa.ebankingver1.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
=======
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
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
<<<<<<< HEAD
@RequestMapping("/api/crypto")
public class CryptoController {

    @Autowired
    private CryptoTradeService tradeService;

    @Autowired
    private CryptoRateService rateService;

    @Autowired
    private BinanceTestnetService binanceTestnetService;

    @Autowired
    private CryptoElasticSyncService elasticService;

    @Autowired
    private UserService userService;

    // ✅ Achat de crypto (débit compte + Binance)
    @PostMapping("/buy")
    public ResponseEntity<?> buyCrypto(Authentication authentication,
                                       @RequestParam String symbol,
                                       @RequestParam double usdAmount) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());

            if (!symbol.endsWith("USDT")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le symbole doit se terminer par USDT (ex: BTCUSDT)"));
            }

            String result = tradeService.buyCrypto(userId, symbol, usdAmount);
            return ResponseEntity.ok(Map.of("message", "Achat réussi", "binanceResponse", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Vente de crypto (crédit compte + Binance)
    @PostMapping("/sell")
    public ResponseEntity<?> sellCrypto(Authentication authentication,
                                        @RequestParam String symbol,
                                        @RequestParam double quantity) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            String result = tradeService.sellCrypto(userId, symbol, quantity);
            return ResponseEntity.ok(Map.of("message", "Vente réussie", "binanceResponse", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/portfolio-value")
    public ResponseEntity<?> getPortfolioValue(Authentication auth) {
        try {
            Long userId = userService.getUserIdByUsername(auth.getName());
            double total = tradeService.getPortfolioValue(userId);
            return ResponseEntity.ok(Map.of("portfolioUsdValue", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCryptoStats(Authentication auth) {
        try {
            Long userId = userService.getUserIdByUsername(auth.getName());
            List<Map<String, Object>> stats = tradeService.getCryptoStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // ✅ Taux unique
    @GetMapping("/rate/{symbol}")
    public ResponseEntity<?> getRate(@PathVariable String symbol) {
        try {
            double rate = rateService.getRate(symbol);
            return ResponseEntity.ok(Map.of("symbol", symbol, "rate", rate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Tous les taux
    @GetMapping("/rates")
    public ResponseEntity<?> getAllRates() {
=======
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
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
        try {
            Map<String, Double> rates = rateService.getAllRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
<<<<<<< HEAD
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Historique crypto depuis Elasticsearch
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            List<CryptoTransaction> history = tradeService.getTransactionHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Recherche filtrée dans l'historique (via Elasticsearch)
    @GetMapping("/search")
    public ResponseEntity<?> searchTransactions(Authentication authentication,
                                                @RequestParam(required = false) String currency,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<Map<String, Object>> results = elasticService.searchTransactions(userId, currency, start, end);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Solde local (peut être amélioré avec portefeuille réel)
    @GetMapping("/balance")
    public ResponseEntity<?> getCryptoBalance(Authentication authentication,
                                              @RequestParam(required = false) String currency) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            Map<String, Double> balances = tradeService.getBalance(userId, currency);
            return ResponseEntity.ok(balances);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Adresse de dépôt Binance Testnet
    @GetMapping("/testnet/address")
    public ResponseEntity<?> getTestnetAddress(@RequestParam String currency) {
        try {
            String address = binanceTestnetService.getDepositAddress(currency);
            return ResponseEntity.ok(Map.of("currency", currency, "address", address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
=======
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
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

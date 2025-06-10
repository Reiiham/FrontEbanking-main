package ma.ensa.ebankingver1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.CryptoTransaction;
import ma.ensa.ebankingver1.model.CryptoWallet;
import ma.ensa.ebankingver1.repository.BankAccountRepository;
import ma.ensa.ebankingver1.repository.CryptoTransactionRepository;
import ma.ensa.ebankingver1.repository.CryptoWalletRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class CryptoTradeService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoTradeService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BinanceTestnetClient binanceClient;
    private final CryptoTransactionRepository txRepo;
    private final CryptoWalletRepository walletRepo;
    private final BankAccountRepository bankAccountRepo;
    private final UserRepository userRepo;
    private final CryptoElasticSyncService elasticService;

    public CryptoTradeService(CryptoTransactionRepository txRepo,
                              CryptoWalletRepository walletRepo,
                              BankAccountRepository bankAccountRepo,
                              UserRepository userRepo,
                              BinanceTestnetClient binanceClient,
                              CryptoElasticSyncService elasticService) {
        this.txRepo = txRepo;
        this.walletRepo = walletRepo;
        this.bankAccountRepo = bankAccountRepo;
        this.userRepo = userRepo;
        this.binanceClient = binanceClient;
        this.elasticService = elasticService;
    }

    @Transactional
    public String buyCrypto(Long userId, String symbol, double usd) throws Exception {
        logger.info("Processing buy for userId: {}, symbol: {}, usd: {}", userId, symbol, usd);
        if (!symbol.endsWith("USDT")) {
            throw new IllegalArgumentException("Symbol must be a USDT trading pair (e.g., BTCUSDT)");
        }
        if (usd <= 0) {
            throw new IllegalArgumentException("USD amount must be positive");
        }

        BankAccount bankAccount = bankAccountRepo.findByUserId(userId).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No bank account found for user ID: " + userId));

        double price = getPrice(symbol);
        double quantity = usd / price;

        if (usd < 10) {
            throw new IllegalArgumentException("USD amount must be at least $10");
        }

        if (bankAccount.getBalance() < usd) {
            throw new RuntimeException("Insufficient bank balance for user ID: " + userId);
        }

        // Adjust quantity to comply with lot size
        String formattedQuantity = adjustQuantity(symbol, quantity);
        logger.debug("Adjusted quantity for {}: {}", symbol, formattedQuantity);

        bankAccount.setBalance(bankAccount.getBalance() - usd);
        bankAccountRepo.save(bankAccount);

        String response = binanceClient.placeOrder(symbol, "BUY", "MARKET", formattedQuantity);
        logger.debug("Binance buy order response: {}", response);

        String currency = symbol.replace("USDT", "");
        CryptoWallet wallet = walletRepo.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> {
                    CryptoWallet w = new CryptoWallet();
                    w.setUser(userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
                    w.setCurrency(currency);
                    w.setBalance(0);
                    return walletRepo.save(w);
                });

        wallet.setBalance(wallet.getBalance() + Double.parseDouble(formattedQuantity));
        walletRepo.save(wallet);

        CryptoTransaction tx = new CryptoTransaction();
        tx.setCurrency(currency);
        tx.setAmount(Double.parseDouble(formattedQuantity));
        tx.setType("BUY");
        tx.setPrice(price);
        tx.setTimestamp(LocalDateTime.now());
        tx.setWallet(wallet);
        txRepo.save(tx);

        // Index transaction in Elasticsearch asynchronously
        elasticService.index(tx).whenComplete((result, error) -> {
            if (error != null) {
                logger.error("Failed to index buy transaction for userId: {}, currency: {}, error: {}",
                        userId, currency, error.getMessage());
            } else {
                logger.debug("Successfully indexed buy transaction for userId: {}, currency: {}",
                        userId, currency);
            }
        });

        return response;
    }

    @Transactional
    public String sellCrypto(Long userId, String symbol, double quantity) throws Exception {
        logger.info("Processing sell for userId: {}, symbol: {}, quantity: {}", userId, symbol, quantity);
        if (!symbol.endsWith("USDT")) {
            throw new IllegalArgumentException("Symbol must be a USDT trading pair (e.g., BTCUSDT)");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        String currency = symbol.replace("USDT", "");
        CryptoWallet wallet = walletRepo.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("No crypto wallet found for user ID: " + userId + " and currency: " + currency));

        if (wallet.getBalance() < quantity) {
            throw new RuntimeException("Insufficient crypto balance for user ID: " + userId);
        }

        BankAccount bankAccount = bankAccountRepo.findByUserId(userId).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No bank account found for user ID: " + userId));

        double price = getPrice(symbol);
        double proceeds = quantity * price;

        if (proceeds < 10) {
            throw new IllegalArgumentException("Sale value must be at least $10");
        }

        String formattedQuantity = adjustQuantity(symbol, quantity);
        logger.debug("Adjusted quantity for {}: {}", symbol, formattedQuantity);

        wallet.setBalance(wallet.getBalance() - Double.parseDouble(formattedQuantity));
        walletRepo.save(wallet);

        bankAccount.setBalance(bankAccount.getBalance() + proceeds);
        bankAccountRepo.save(bankAccount);

        String response = binanceClient.placeOrder(symbol, "SELL", "MARKET", formattedQuantity);
        logger.debug("Binance sell order response: {}", response);

        CryptoTransaction tx = new CryptoTransaction();
        tx.setCurrency(currency);
        tx.setAmount(Double.parseDouble(formattedQuantity));
        tx.setType("SELL");
        tx.setPrice(price);
        tx.setTimestamp(LocalDateTime.now());
        tx.setWallet(wallet);
        txRepo.save(tx);

        // Index transaction in Elasticsearch asynchronously
        elasticService.index(tx).whenComplete((result, error) -> {
            if (error != null) {
                logger.error("Failed to index sell transaction for userId: {}, currency: {}, error: {}",
                        userId, currency, error.getMessage());
            } else {
                logger.debug("Successfully indexed sell transaction for userId: {}, currency: {}",
                        userId, currency);
            }
        });

        return response;
    }

    public double getPrice(String symbol) throws Exception {
        String json = binanceClient.getTickerPrice(symbol);
        logger.debug("Raw Binance price response: {}", json);
        JsonNode root = objectMapper.readTree(json);
        String priceStr = root.path("price").asText();
        if (priceStr.isEmpty()) {
            throw new RuntimeException("Invalid price response from Binance for symbol: " + symbol);
        }
        return Double.parseDouble(priceStr);
    }

    private String adjustQuantity(String symbol, double quantity) throws Exception {
        String exchangeInfo = binanceClient.getExchangeInfo(symbol);
        JsonNode root = objectMapper.readTree(exchangeInfo);
        JsonNode symbolInfo = root.path("symbols").get(0);
        JsonNode lotSizeFilter = null;
        for (Iterator<JsonNode> it = symbolInfo.path("filters").elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            if (node.path("filterType").asText().equals("LOT_SIZE")) {
                lotSizeFilter = node;
                break;
            }
        }
        if (lotSizeFilter == null) {
            throw new RuntimeException("LOT_SIZE filter not found for " + symbol);
        }

        BigDecimal minQty = new BigDecimal(lotSizeFilter.path("minQty").asText());
        BigDecimal stepSize = new BigDecimal(lotSizeFilter.path("stepSize").asText());
        BigDecimal qty = new BigDecimal(String.valueOf(quantity));

        // Round down to nearest step size
        BigDecimal remainder = qty.remainder(stepSize);
        qty = qty.subtract(remainder);
        if (qty.compareTo(minQty) < 0) {
            throw new IllegalArgumentException("Quantity " + qty + " below minimum " + minQty + " for " + symbol);
        }

        // Format to required precision
        int precision = stepSize.scale();
        return qty.setScale(precision, RoundingMode.DOWN).toString();
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getBalance(Long userId, String currency) throws Exception {
        logger.info("Fetching balance for userId: {}, currency: {}", userId, currency);

        // Validate user exists
        if (!userRepo.existsById(userId)) {
            throw new RuntimeException("User not found for ID: " + userId);
        }

        Map<String, Double> balanceMap = new HashMap<>();

        // Fetch wallets based on currency
        List<CryptoWallet> wallets;
        if (currency != null && !currency.isEmpty()) {
            // Validate currency format
            if (!currency.matches("[A-Z]{3,5}")) {
                throw new IllegalArgumentException("Invalid currency format: " + currency);
            }
            CryptoWallet wallet = walletRepo.findByUserIdAndCurrency(userId, currency)
                    .orElseThrow(() -> new RuntimeException("No wallet found for user ID: " + userId + " and currency: " + currency));
            wallets = List.of(wallet);
        } else {
            wallets = walletRepo.findByUserId(userId);
            if (wallets.isEmpty()) {
                throw new RuntimeException("No wallets found for user ID: " + userId);
            }
        }

        // Calculate balance and USD value for each wallet
        for (CryptoWallet wallet : wallets) {
            String curr = wallet.getCurrency();
            double balance = wallet.getBalance();
            String symbol = curr + "USDT";

            try {
                double price = getPrice(symbol);
                double usdValue = balance * price;
                balanceMap.put(curr + "_balance", balance);
                balanceMap.put(curr + "_usd", usdValue);
                logger.debug("Balance for {}: {} {}, USD value: {}", curr, balance, curr, usdValue);
            } catch (Exception e) {
                logger.error("Failed to fetch price for symbol {}: {}", symbol, e.getMessage());
                throw new RuntimeException("Unable to fetch price for " + curr + ": " + e.getMessage());
            }
        }

        return balanceMap;
    }
}

package ma.ensa.ebankingver1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CryptoRateService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoRateService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public double getRate(String symbol) {
        // symbol ex: BTCUSDT, ETHUSDT
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol.toUpperCase();

        try {
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("price")) {
                String msg = "Taux non disponible pour : " + symbol;
                logger.error(msg);
                throw new RuntimeException(msg);
            }

            return Double.parseDouble(response.get("price"));

        } catch (RestClientException | NumberFormatException e) {
            String msg = "Erreur lors de la récupération du taux pour : " + symbol + " - " + e.getMessage();
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
    public Map<String, Double> getAllRates() {
        String url = "https://api.binance.com/api/v3/ticker/price";
        try {
            List<Map<String, String>> response = restTemplate.getForObject(url, List.class);
            Map<String, Double> rates = new HashMap<>();
            if (response != null) {
                for (Map<String, String> entry : response) {
                    String symbol = entry.get("symbol");
                    if (symbol.endsWith("USDT")) {
                        rates.put(symbol, Double.parseDouble(entry.get("price")));
                    }
                }
            }
            return rates;
        } catch (RestClientException | NumberFormatException e) {
            logger.error("Failed to fetch rates: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}


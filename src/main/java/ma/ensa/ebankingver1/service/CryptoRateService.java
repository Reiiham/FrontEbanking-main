package ma.ensa.ebankingver1.service;

<<<<<<< HEAD
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

@Service
public class CryptoRateService {

<<<<<<< HEAD
    @Autowired
    private RestTemplate restTemplate;

    private static final List<String> SYMBOLS = List.of("BTCUSDT","ETHUSDT");

    public double getRate(String symbol) {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol;
        Map<String,String> resp = restTemplate.getForObject(url, Map.class);
        return Double.parseDouble(resp.get("price"));
    }

    public Map<String, Double> getAllRates() {
        return SYMBOLS.stream()
                .collect(Collectors.toMap(s -> s, this::getRate));
    }
}
=======
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

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

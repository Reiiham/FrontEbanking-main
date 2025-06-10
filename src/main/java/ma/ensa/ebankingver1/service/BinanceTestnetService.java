package ma.ensa.ebankingver1.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
@PropertySource("classpath:application.properties")

@Service
public class BinanceTestnetService {

    @Value("${binance.testnet.apiKey}")
    private String apiKey;
    @Value("${binance.testnet.secret}")
    private String secretKey;
    @Value("${binance.testnet.baseUrl}")
    private String baseUrl;

    private HttpClient client;

    @PostConstruct
    public void init() {
        client = HttpClient.newHttpClient();
    }

    public String placeOrder(String symbol, String side, String type, double quantity, double price) throws Exception {
        long ts = Instant.now().toEpochMilli();
        String qs = "symbol=" + symbol +
                "&side=" + side +
                "&type=" + type +
                "&timeInForce=GTC" +
                "&quantity=" + quantity +
                "&price=" + price +
                "&recvWindow=5000" +
                "&timestamp=" + ts;
        String sig = sign(qs);
        URI uri = URI.create(baseUrl + "/api/v3/order?" + qs + "&signature=" + sig);

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("X-MBX-APIKEY", apiKey)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String getDepositAddress(String currency) throws Exception {
        long ts = Instant.now().toEpochMilli();
        String qs = "coin=" + currency + "&timestamp=" + ts;
        String sig = sign(qs);
        URI uri = URI.create(baseUrl + "/sapi/v1/capital/deposit/address?" + qs + "&signature=" + sig);

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

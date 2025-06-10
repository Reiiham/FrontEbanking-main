package ma.ensa.ebankingver1.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BinanceTestnetClient {
    private final String apiKey;
    private final String secretKey;
    private final String baseUrl;
    private final OkHttpClient client = new OkHttpClient();

    public BinanceTestnetClient(
            @Value("${binance.testnet.api-key}") String apiKey,
            @Value("${binance.testnet.secret-key}") String secretKey,
            @Value("${binance.testnet.base-url:https://testnet.binance.vision}") String baseUrl) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
    }

    private String sign(String data) throws Exception {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        sha256.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
        byte[] hash = sha256.doFinal(data.getBytes());
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public String placeOrder(String symbol, String side, String type, String quantity) throws Exception {
        long timestamp = System.currentTimeMillis();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("type", type);
        params.put("quantity", quantity);
        params.put("timestamp", String.valueOf(timestamp));

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-8")).append('&');
        }
        query.deleteCharAt(query.length() - 1);

        String signature = sign(query.toString());
        String fullQuery = query + "&signature=" + signature;

        Request request = new Request.Builder()
                .url(baseUrl + "/api/v3/order?" + fullQuery)
                .post(RequestBody.create(null, new byte[0]))
                .addHeader("X-MBX-APIKEY", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to place order: " + response.body().string());
            }
            return response.body().string();
        }
    }

    public String getTickerPrice(String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/v3/ticker/price?symbol=" + symbol)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to get ticker price: " + response.body().string());
            }
            return response.body().string();
        }
    }

    public String getDepositAddress(String currency) throws Exception {
        long timestamp = System.currentTimeMillis();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("coin", currency);
        params.put("network", "BTC");
        params.put("timestamp", String.valueOf(timestamp));

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-8")).append('&');
        }
        query.deleteCharAt(query.length() - 1);

        String signature = sign(query.toString());
        String fullQuery = query + "&signature=" + signature;

        Request request = new Request.Builder()
                .url(baseUrl + "/sapi/v1/capital/deposit/address?" + fullQuery)
                .get()
                .addHeader("X-MBX-APIKEY", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to get deposit address: " + response.body().string());
            }
            return response.body().string();
        }
    }

    public String getExchangeInfo(String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/v3/exchangeInfo?symbol=" + symbol)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to get exchange info: " + response.body().string());
            }
            return response.body().string();
        }
    }
}
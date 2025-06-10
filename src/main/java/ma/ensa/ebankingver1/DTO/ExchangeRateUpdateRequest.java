package ma.ensa.ebankingver1.DTO;

import jakarta.validation.constraints.Min;

public class ExchangeRateUpdateRequest {
    @Min(value = 0, message = "Le taux de change doit être positif.")
    private double exchangeRate;

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}




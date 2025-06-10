package ma.ensa.ebankingver1.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WitAIResponse {
    @JsonProperty("intents")
    private Intent[] intents;

    @JsonProperty("entities")
    private Map<String, Entity[]> entities;

    public String getIntent() {
        return intents != null && intents.length > 0 ? intents[0].name : null;
    }

    public Map<String, Object> getEntities() {
        Map<String, Object> result = new HashMap<>();
        if (entities != null) {
            entities.forEach((key, value) -> {
                if (value != null && value.length > 0 && value[0].value != null) {
                    String entityName = key.split(":")[0];
                    Object entityValue = value[0].value;
                    if (entityName.equals("amount_of_money")) {
                        if (entityValue instanceof Map) {
                            Map<String, Object> amountData = (Map<String, Object>) entityValue;
                            result.put(entityName, amountData.getOrDefault("amount", 0.0));
                        } else if (entityValue instanceof Number) {
                            result.put(entityName, ((Number) entityValue).doubleValue());
                        }
                    } else {
                        result.put(entityName, String.valueOf(entityValue));
                    }
                }
            });
        }
        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Intent {
        @JsonProperty("name")
        String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Entity {
        @JsonProperty("value")
        Object value;

        @JsonProperty("body")
        String body;
    }
}

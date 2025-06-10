package ma.ensa.ebankingver1.controller;

import jakarta.validation.Valid;
import ma.ensa.ebankingver1.DTO.EmployeeDTO;
import ma.ensa.ebankingver1.DTO.ExchangeRateUpdateRequest;
import ma.ensa.ebankingver1.DTO.SettingUpdateRequest;
import ma.ensa.ebankingver1.model.Currency;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.service.AuditService;
import ma.ensa.ebankingver1.service.CurrencyService;
import ma.ensa.ebankingver1.service.EmployeeService;
import ma.ensa.ebankingver1.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private SettingService settingService; // Injection de SettingService
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AuditService auditService;


    @GetMapping("/currencies")
    public ResponseEntity<List<Currency>> getCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        logger.info("Fetched all currencies");
        auditService.logAction("GET_CURRENCIES", "CURRENCY", null, Map.of("count", currencies.size()), true);
        return ResponseEntity.ok(currencies);
    }

    @PostMapping("/currencies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Currency> addCurrency(@RequestBody Currency currency) {
        try {
            logger.info("Adding new currency: {}", currency.getCodeISO());
            Currency savedCurrency = currencyService.addCurrency(currency);
            auditService.logAction("ADD_CURRENCY", "CURRENCY", String.valueOf(savedCurrency.getId()), Map.of("codeISO", currency.getCodeISO()), true);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCurrency); // Retourne 201 si succès
        } catch (IllegalArgumentException e) {
            logger.error("Failed to add currency: {}", e.getMessage());
            auditService.logAction("ADD_CURRENCY_FAILED", "CURRENCY", null, Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Retourne 409 en cas de conflit
        } catch (Exception e) {
            logger.error("Internal error adding currency: {}", e.getMessage());
            auditService.logAction("ADD_CURRENCY_FAILED", "CURRENCY", null, Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retourne 500 en cas d'erreur
        }
    }
    @PutMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Currency> updateCurrency(@PathVariable("id") Long id, @RequestBody Currency currency) {
        try {
            logger.info("Updating currency with ID: {}", id);
            Currency updatedCurrency = currencyService.updateCurrency(id, currency);
            auditService.logAction("UPDATE_CURRENCY", "CURRENCY", String.valueOf(id), Map.of("codeISO", currency.getCodeISO()), true);
            return ResponseEntity.ok(updatedCurrency);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update currency: {}", e.getMessage());
            auditService.logAction("UPDATE_CURRENCY_FAILED", "CURRENCY", String.valueOf(id), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PutMapping("/currencies/{id}/exchange-rate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Currency> updateExchangeRate(@PathVariable("id") Long id, @Valid @RequestBody ExchangeRateUpdateRequest request) {
        try {
            logger.info("Updating exchange rate for currency ID: {}", id);
            Currency updatedCurrency = currencyService.updateExchangeRate(id, request.getExchangeRate());
            auditService.logAction("UPDATE_EXCHANGE_RATE", "CURRENCY", String.valueOf(id), Map.of("rate", request.getExchangeRate()), true);
            return ResponseEntity.ok(updatedCurrency);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update exchange rate: {}", e.getMessage());
            auditService.logAction("UPDATE_EXCHANGE_RATE_FAILED", "CURRENCY", String.valueOf(id), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCurrency(@PathVariable("id") Long id) {
        try {
            logger.info("Deleting currency with ID: {}", id);
            currencyService.deleteCurrency(id);
            auditService.logAction("DELETE_CURRENCY", "CURRENCY", String.valueOf(id), new HashMap<>(), true);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete currency: {}", e.getMessage());
            auditService.logAction("DELETE_CURRENCY_FAILED", "CURRENCY", String.valueOf(id), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        logger.info("Admin dashboard accessed");
        auditService.logAction("ACCESS_DASHBOARD", "ADMIN", null, Map.of("endpoint", "/api/admin/dashboard"), true);
        return Map.of(
                "totalCurrencies", currencyService.getAllCurrencies().size(),
                "totalSettings", settingService.getAllSettings().size(),
                "lastUpdate", LocalDateTime.now().toString()
        );
    }
    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            logger.info("Creating new employee: {}", employeeDTO.getFirstName());
            User employee = employeeService.createEmployee(employeeDTO);
            auditService.logAction("CREATE_EMPLOYEE", "USER", String.valueOf(employee.getId()), Map.of("email", employee.getEmail()), true);
            return new ResponseEntity<>(employee, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create employee: {}", e.getMessage());
            auditService.logAction("CREATE_EMPLOYEE_FAILED", "USER", null, Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal error creating employee: {}", e.getMessage());
            auditService.logAction("CREATE_EMPLOYEE_FAILED", "USER", null, Map.of("error", e.getMessage()), false);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne");
        }
    }
    @GetMapping("/audit/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        try {
            Map<String, Object> stats = auditService.getAuditStats();
            auditService.logAction("VIEW_AUDIT_STATS", "AUDIT", null, stats, true);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to retrieve audit stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve audit stats: " + e.getMessage()));
        }
    }

    /*

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private SettingService globalSettingService;
    @Autowired
    private SettingService settingService;

    // Gestion des devises
    //get currencies
    //MARCHE
    @GetMapping("/currencies")
    public List<Currency> getCurrencies() {
        return currencyService.getAllCurrencies();
    }

    //MARCHE
    @PostMapping("/currencies")
    //@PreAuthorize("hasRole('ADMIN')")
    public Currency addCurrency(@RequestBody Currency currency) {
        return currencyService.saveCurrency(currency);
    }

    //MARCHE
    @PutMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Currency updateCurrency(@PathVariable("id") Long id, @RequestBody Currency currency) {
        return currencyService.updateCurrency(id, currency);
    }

    // MARCHE ET RETOURNE JSON
    //n est pas encore ajouté dans angular
    @PutMapping("/currencies/{id}/exchange-rate")
    @PreAuthorize("hasRole('ADMIN')")
    public Currency updateExchangeRate(@PathVariable("id") Long id, @Valid @RequestBody ExchangeRateUpdateRequest request) {
        return currencyService.updateExchangeRate(id, request.getExchangeRate());
    }
//ne retourne rien mais marche

    @DeleteMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCurrency(@PathVariable("id") Long id) {
        currencyService.deleteCurrency(id);
    }

    // Gestion des paramètres globaux
    //MARCHE
    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<GlobalSetting> getAllSettings() {
        return globalSettingService.getAllSettings();
    }
//il retourne un true text brut mais on va utiliser angular apres donc json



    // marche
@GetMapping("/settings/{key}")
@PreAuthorize("hasRole('ADMIN')")
public Map<String, String> getSettingValue(@PathVariable(name = "key") String key) {
    return Map.of("value", globalSettingService.getSettingValue(key));
}

//endpoint pour dashbord
@GetMapping("/dashboard")
public Map<String, Object> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalCurrencies", currencyService.getAllCurrencies().size());
    stats.put("totalSettings", settingService.getAllSettings().size());
    stats.put("lastUpdate", LocalDate.now().toString());
    return stats;
}




    /*
    @PutMapping("/settings/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalSetting updateSetting(@PathVariable String key, @RequestBody String value) {
        return globalSettingService.updateSetting(key, value);
    }


    //pour utiliser que json
    // MARCHE ET RETOURNE JSON
    @PutMapping("/settings/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalSetting updateSetting(@PathVariable (name = "key") String key, @RequestBody SettingUpdateRequest request) {
          return globalSettingService.updateSetting(key, request.getValue());
}
*/

}
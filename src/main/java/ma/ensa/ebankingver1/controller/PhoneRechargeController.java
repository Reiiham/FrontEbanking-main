package ma.ensa.ebankingver1.controller;

import ma.ensa.ebankingver1.DTO.PhoneRechargeRequest;
import ma.ensa.ebankingver1.DTO.PhoneRechargeResponse;
import ma.ensa.ebankingver1.model.PhoneRecharge;
import ma.ensa.ebankingver1.service.PhoneRechargeService;
import ma.ensa.ebankingver1.service.BankAccountService;
import ma.ensa.ebankingver1.service.UserService;
import ma.ensa.ebankingver1.service.OperatorApiService;
import ma.ensa.ebankingver1.model.BankAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/client/recharge")
@CrossOrigin(origins = "http://localhost:4200")
public class PhoneRechargeController {

    private static final Logger logger = LoggerFactory.getLogger(PhoneRechargeController.class);

    @Autowired
    private PhoneRechargeService rechargeService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperatorApiService operatorApiService;

    /**
     * Effectuer une recharge téléphonique avec Reloadly
     */
    @PostMapping("/phone")
    public ResponseEntity<PhoneRechargeResponse> rechargePhone(
            @Valid @RequestBody PhoneRechargeRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            logger.info("Processing recharge request for user: {}, operator: {}, phone: {}, amount: {}",
                    username, request.getOperatorCode(), request.getPhoneNumber(), request.getAmount());

            PhoneRechargeResponse response = rechargeService.processRecharge(request, username);

            if ("SUCCESS".equals(response.getStatus()) || "PENDING".equals(response.getStatus())) {
                logger.info("Recharge successful for user: {}, transaction: {}", username, response.getTransactionReference());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Recharge failed for user: {}, reason: {}", username, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("System error during recharge for user: {}", authentication.getName(), e);
            PhoneRechargeResponse errorResponse = new PhoneRechargeResponse("Erreur système: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Tester la connexion avec Reloadly
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testReloadlyConnection() {
        try {
            Map<String, Object> result = operatorApiService.testConnection();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Erreur de connexion Reloadly: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }

    /**
     * Obtenir les opérateurs disponibles depuis Reloadly pour le Maroc
     */
    @GetMapping("/operators")
    public ResponseEntity<List<Map<String, Object>>> getSupportedOperators() {
        try {
            List<Map<String, Object>> operators = List.of(
                    Map.of(
                            "code", "IAM",
                            "name", "Maroc Telecom (IAM)",
                            "logo", "/assets/iam-logo.png",
                            "description", "Recharge via Reloadly",
                            "minAmount", 5,
                            "maxAmount", 500,
                            "currency", "MAD"
                    ),
                    Map.of(
                            "code", "ORANGE",
                            "name", "Orange Maroc",
                            "logo", "/assets/orange-logo.png",
                            "description", "Recharge via Reloadly",
                            "minAmount", 5,
                            "maxAmount", 500,
                            "currency", "MAD"
                    ),
                    Map.of(
                            "code", "INWI",
                            "name", "Inwi",
                            "logo", "/assets/inwi-logo.png",
                            "description", "Recharge via Reloadly",
                            "minAmount", 5,
                            "maxAmount", 500,
                            "currency", "MAD"
                    )
            );
            return ResponseEntity.ok(operators);
        } catch (Exception e) {
            logger.error("Error getting operators", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * Obtenir les montants prédéfinis optimisés pour Reloadly
     */
    @GetMapping("/amounts")
    public ResponseEntity<Map<String, Object>> getPredefinedAmounts() {
        Map<String, Object> amountsInfo = new HashMap<>();
        amountsInfo.put("amounts", List.of(5, 10, 20, 50, 100, 200, 500));
        amountsInfo.put("currency", "MAD");
        amountsInfo.put("minAmount", 5);
        amountsInfo.put("maxAmount", 500);
        amountsInfo.put("note", "Montants en mode sandbox Reloadly gratuit");

        return ResponseEntity.ok(amountsInfo);
    }

    /**
     * Valider un numéro de téléphone marocain
     */
    @PostMapping("/validate-phone")
    public ResponseEntity<Map<String, Object>> validatePhoneNumber(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String operatorCode = request.get("operatorCode");

        Map<String, Object> result = new HashMap<>();

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "Numéro de téléphone requis");
            return ResponseEntity.badRequest().body(result);
        }

        // Validation du format marocain
        boolean isValid = isValidMoroccanPhoneNumber(phoneNumber, operatorCode);
        result.put("valid", isValid);
        result.put("message", isValid ? "Numéro valide" : "Format de numéro invalide pour l'opérateur " + operatorCode);
        result.put("phoneNumber", phoneNumber);
        result.put("operatorCode", operatorCode);

        return ResponseEntity.ok(result);
    }

    /**
     * Obtenir l'historique des recharges avec informations Reloadly
     */
    @GetMapping("/history")
    public ResponseEntity<?> getRechargeHistory(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);

            List<BankAccount> userAccounts = bankAccountService.findByUserId(userId);
            if (userAccounts.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Aucun compte bancaire trouvé pour cet utilisateur")
                );
            }

            String accountRib = userAccounts.get(0).getRib();
            List<PhoneRecharge> history = rechargeService.getRechargeHistory(accountRib);

            // Enrichir l'historique avec des informations sur le mode sandbox
            Map<String, Object> response = new HashMap<>();
            response.put("history", history);
            response.put("mode", "sandbox");
            response.put("provider", "Reloadly");
            response.put("totalTransactions", history.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting recharge history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'historique: " + e.getMessage()));
        }
    }

    /**
     * Obtenir les informations du compte avec limites Reloadly
     */
    @GetMapping("/account-info")
    public ResponseEntity<?> getAccountInfo(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);

            List<BankAccount> userAccounts = bankAccountService.findByUserId(userId);
            if (userAccounts.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Aucun compte bancaire trouvé")
                );
            }

            BankAccount account = userAccounts.get(0);
            Map<String, Object> accountInfo = new HashMap<>();
            accountInfo.put("accountNumber", account.getRib());
            accountInfo.put("balance", account.getBalance());
            accountInfo.put("accountType", account.getType() != null ? account.getType() : "CHECKING");
            accountInfo.put("rechargeProvider", "Reloadly Sandbox");
            accountInfo.put("supportedOperators", List.of("IAM", "ORANGE", "INWI"));
            accountInfo.put("maxRechargeAmount", 500);
            accountInfo.put("currency", "MAD");

            return ResponseEntity.ok(accountInfo);
        } catch (Exception e) {
            logger.error("Error getting account info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des informations du compte"));
        }
    }

    /**
     * Obtenir les limites spécifiques à Reloadly sandbox
     */
    @GetMapping("/daily-limits")
    public ResponseEntity<?> getDailyLimits(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);

            List<BankAccount> userAccounts = bankAccountService.findByUserId(userId);
            if (userAccounts.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Aucun compte bancaire trouvé")
                );
            }

            // Limites pour le mode sandbox Reloadly
            Map<String, Object> limits = new HashMap<>();
            limits.put("dailyLimit", 1000);
            limits.put("remainingLimit", 1000); // En sandbox, généralement illimité
            limits.put("transactionLimit", 500);
            limits.put("currency", "MAD");
            limits.put("mode", "sandbox");
            limits.put("provider", "Reloadly");
            limits.put("note", "Limites en mode test gratuit");

            return ResponseEntity.ok(limits);
        } catch (Exception e) {
            logger.error("Error getting daily limits", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des limites"));
        }
    }

    /**
     * Endpoint de debug (à supprimer en production)
     */
    @GetMapping("/debug-pin")
    public ResponseEntity<?> debugPin(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);
            List<BankAccount> accounts = bankAccountService.findByUserId(userId);

            if (!accounts.isEmpty()) {
                BankAccount account = accounts.get(0);
                Map<String, Object> debugInfo = new HashMap<>();
                debugInfo.put("username", username);
                debugInfo.put("rib", account.getRib());
                debugInfo.put("transactionPin", account.getTransactionPin()); // DANGER : uniquement pour debug !
                debugInfo.put("balance", account.getBalance());
                debugInfo.put("reloadlyMode", "sandbox");
                debugInfo.put("warning", "DEBUG MODE - Remove in production!");

                return ResponseEntity.ok(debugInfo);
            }
            return ResponseEntity.badRequest().body("No accounts found");
        } catch (Exception e) {
            logger.error("Debug error", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtenir le statut du service Reloadly
     */
    @GetMapping("/service-status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            Map<String, Object> connectionTest = operatorApiService.testConnection();
            status.put("reloadlyConnection", connectionTest);
            status.put("serviceAvailable", "SUCCESS".equals(connectionTest.get("status")));
            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("serviceAvailable", false);
            status.put("error", e.getMessage());
            status.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }

    // Méthodes utilitaires
    private boolean isValidMoroccanPhoneNumber(String phoneNumber, String operatorCode) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Nettoyer le numéro
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Vérifier le format marocain
        if (cleanNumber.startsWith("+212")) {
            cleanNumber = "0" + cleanNumber.substring(4);
        } else if (cleanNumber.startsWith("212")) {
            cleanNumber = "0" + cleanNumber.substring(3);
        }

        // Vérifier la longueur (10 chiffres)
        if (cleanNumber.length() != 10 || !cleanNumber.startsWith("0")) {
            return false;
        }

        // Vérifier les préfixes selon l'opérateur
        switch (operatorCode) {
            case "IAM":
                return cleanNumber.matches("^0(6[0-6]|7[0-1])\\d{7}$");
            case "ORANGE":
                return cleanNumber.matches("^0(6[7-9])\\d{7}$");
            case "INWI":
                return cleanNumber.matches("^0(6[5-6])\\d{7}$");
            default:
                return cleanNumber.matches("^0[67]\\d{8}$"); // Format général mobile marocain
        }
    }
}
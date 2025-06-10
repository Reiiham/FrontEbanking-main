package ma.ensa.ebankingver1.controller;

import jakarta.validation.*;
import ma.ensa.ebankingver1.DTO.ResendActivationRequest;
import ma.ensa.ebankingver1.DTO.SetPasswordRequest;
import ma.ensa.ebankingver1.service.AuditService;
import ma.ensa.ebankingver1.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


import java.util.Map;
@RestController
@RequestMapping("/api/auth")
public class PasswordController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuditService auditService;

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    // Endpoint pour vérifier la validité d'un token - VERSION DEBUG
    @GetMapping("/validate-token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable("token") String token) {
        logger.info("=== VALIDATE TOKEN START ===");
        logger.info("Received token: {}", token);
        logger.info("Token length: {}", token != null ? token.length() : "null");

        try {
            // Vérification de base
            if (token == null || token.trim().isEmpty()) {
                logger.error("Token is null or empty");
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Token vide"
                ));
            }

            logger.info("Calling employeeService.isTokenValid...");
            boolean isValid = employeeService.isTokenValid(token);
            logger.info("Token validation result: {}", isValid);

            if (isValid) {
                logger.info("Token is valid - returning success");
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token valide"
                ));
            } else {
                logger.warn("Token is invalid or expired - returning failure");
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Token invalide ou expiré"
                ));
            }

        } catch (Exception e) {
            logger.error("=== EXCEPTION IN VALIDATE TOKEN ===");
            logger.error("Exception type: {}", e.getClass().getSimpleName());
            logger.error("Exception message: {}", e.getMessage());
            logger.error("Stack trace: ", e);

            // Retourner une réponse JSON même en cas d'exception
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Erreur lors de la validation du token: " + e.getMessage()
            ));
        } finally {
            logger.info("=== VALIDATE TOKEN END ===");
        }
    }

    // Endpoint pour définir le mot de passe avec le token
    @PostMapping("/set-password")
    public ResponseEntity<?> setPasswordWithToken(@Valid @RequestBody SetPasswordRequest request) {
        try {
            logger.info("Setting password with token: {}", request.getToken().substring(0, 8) + "...");
            employeeService.setPasswordWithToken(request.getToken(), request.getNewPassword());
            auditService.logAction("SET_PASSWORD_SUCCESS", "USER", null,
                    Map.of("token_prefix", request.getToken().substring(0, 8)), true);
            return ResponseEntity.ok(Map.of("message", "Mot de passe défini avec succès"));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to set password: {}", e.getMessage());
            auditService.logAction("SET_PASSWORD_FAILED", "USER", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Internal error setting password: {}", e.getMessage());
            auditService.logAction("SET_PASSWORD_FAILED", "USER", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne"));
        }
    }

    // Endpoint pour renvoyer un lien d'activation
    @PostMapping("/resend-activation")
    public ResponseEntity<?> resendActivationLink(@Valid @RequestBody ResendActivationRequest request) {
        try {
            logger.info("Resending activation link to: {}", request.getEmail());
            employeeService.resendActivationLink(request.getEmail());
            auditService.logAction("RESEND_ACTIVATION", "USER", null,
                    Map.of("email", request.getEmail()), true);
            return ResponseEntity.ok(Map.of("message", "Lien d'activation renvoyé"));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to resend activation: {}", e.getMessage());
            auditService.logAction("RESEND_ACTIVATION_FAILED", "USER", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Internal error resending activation: {}", e.getMessage());
            auditService.logAction("RESEND_ACTIVATION_FAILED", "USER", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne"));
        }
    }
}
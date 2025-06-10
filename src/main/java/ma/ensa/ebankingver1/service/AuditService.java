package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.AuditLog;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void testInsert() {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(null);
        auditLog.setAction("TEST_INSERT");
        auditLog.setEntityType("TEST");
        auditLog.setEntityId("TEST_ID");
        auditLog.setDetails("{\"test\": \"manual\"}");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setSuccess(true);
        logger.info("Testing audit log save: {}", auditLog);
        auditLogRepository.saveAndFlush(auditLog);
        logger.info("Test audit log saved");
    }

    @Transactional
    public void logAction(String action, String entityType, String entityId, Map<String, Object> details, boolean success) {
        logger.info("Attempting to log action: {} - {} - {}", action, entityType, entityId);
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(mapToJson(details));
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setSuccess(success);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            auditLog.setUser(user);
            logger.debug("User found for audit log: {}", user != null ? user.getEmail() : "null");
        } else {
            logger.warn("No authenticated user found for audit log: {}", action);
        }

        try {
            auditLogRepository.saveAndFlush(auditLog);
            logger.info("Audit log saved: {} - {} - {} - Success: {}", action, entityType, entityId, success);
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save audit log", e);
        }
    }

    private String mapToJson(Map<String, Object> details) {
        if (details == null || details.isEmpty()) return "{}";
        StringBuilder json = new StringBuilder("{");
        details.forEach((k, v) -> json.append("\"").append(k).append("\":\"").append(v).append("\","));
        json.setLength(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    public Map<String, Object> getAuditStats() {
        long totalLogs = auditLogRepository.count();
        long successfulLogs = auditLogRepository.countBySuccessTrue();
        long failedLogs = totalLogs - successfulLogs;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", totalLogs);
        stats.put("successfulLogs", successfulLogs);
        stats.put("failedLogs", failedLogs);
        return stats;
    }

    public long countBySuccessTrue() {
        return auditLogRepository.countBySuccessTrue();
    }
}
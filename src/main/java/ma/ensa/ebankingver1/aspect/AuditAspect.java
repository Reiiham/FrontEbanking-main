package ma.ensa.ebankingver1.aspect;

import ma.ensa.ebankingver1.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditService auditService;

    @AfterReturning(pointcut = "execution(* ma.ensa.ebankingver1.service..*.*(..)) && !within(ma.ensa.ebankingver1.service.AuditService)", returning = "result")
    public void logSuccess(JoinPoint joinPoint, Object result) {
        logger.info("AOP triggered for successful method: {}", joinPoint.getSignature().toString());
        String methodName = joinPoint.getSignature().getName();
        String entityType = inferEntityType(joinPoint);
        String entityId = extractEntityId(joinPoint, result);

        Map<String, Object> details = new HashMap<>();
        details.put("method", methodName);
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            details.put("entityId", args[0]);
            if (args.length > 1 && args[1] instanceof Double) details.put("amount", args[1]);
            if (args.length > 2 && args[2] instanceof String) details.put("description", args[2]);
        }
        details.put("result", result != null ? result.toString() : "null");

        auditService.logAction(methodName, entityType, entityId, details, true);
    }

    @AfterThrowing(pointcut = "execution(* ma.ensa.ebankingver1.service..*.*(..)) && !within(ma.ensa.ebankingver1.service.AuditService)", throwing = "exception")
    public void logFailure(JoinPoint joinPoint, Exception exception) {
        logger.info("AOP triggered for failed method: {}", joinPoint.getSignature().toString());
        String methodName = joinPoint.getSignature().getName();
        String entityType = inferEntityType(joinPoint);
        String entityId = extractEntityId(joinPoint, null);

        Map<String, Object> details = new HashMap<>();
        details.put("method", methodName);
        details.put("error", exception.getMessage());
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            details.put("entityId", args[0]);
            if (args.length > 1 && args[1] instanceof Double) details.put("amount", args[1]);
            if (args.length > 2 && args[2] instanceof String) details.put("description", args[2]);
        }

        auditService.logAction(methodName, entityType, entityId, details, false);
    }

    private String inferEntityType(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        if (className.contains("BankAccount")) return "BANK_ACCOUNT";
        return className.replace("ServiceImpl", "").replace("Service", "").toUpperCase();
    }

    private String extractEntityId(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        return args.length > 0 ? args[0].toString() : "N/A";
    }
}
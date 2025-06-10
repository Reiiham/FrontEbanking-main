package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    long countBySuccessTrue();
}
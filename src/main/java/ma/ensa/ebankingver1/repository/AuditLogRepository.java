package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD

=======
import org.springframework.stereotype.Repository;

@Repository
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    long countBySuccessTrue();
}
package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.SuspendedService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuspendedServiceRepository extends JpaRepository<SuspendedService, Long> {
    List<SuspendedService> findByUserId(Long userId);
}
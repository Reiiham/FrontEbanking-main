// src/main/java/ma.ensa.ebankingver1/repository/BeneficiaryRepository.java
package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
    List<Beneficiary> findByClientId(Long clientId);
    Optional<Beneficiary> findByRib(String rib); // Changed to Optional<Beneficiary>
}
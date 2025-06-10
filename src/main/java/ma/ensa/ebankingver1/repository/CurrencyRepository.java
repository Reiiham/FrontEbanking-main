package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency findByIsDefaultTrue();
    boolean existsByCodeISO(String codeISO);
    Optional<Currency> findByCodeISO(String codeISO);


}

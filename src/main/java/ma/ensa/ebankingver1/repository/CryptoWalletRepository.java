package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.CryptoWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
    Optional<CryptoWallet> findByUserIdAndCurrency(Long userId, String currency);

    List<CryptoWallet> findByUserId(Long userId);
}

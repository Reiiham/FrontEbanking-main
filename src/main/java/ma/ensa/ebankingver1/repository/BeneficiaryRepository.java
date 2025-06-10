// src/main/java/ma.ensa.ebankingver1/repository/BeneficiaryRepository.java
package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
<<<<<<< HEAD
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserIdAndActif(Long userId, Boolean actif);
    List<Beneficiary> findByUserId(Long userId);
    boolean existsByUserIdAndRib(Long userId, String rib);
    Optional<Beneficiary> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Beneficiary b WHERE b.user.id = :userId AND b.actif = true AND " +
            "(LOWER(b.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.prenom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.surnom) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Beneficiary> searchByUserIdAndName(@Param("userId") Long userId, @Param("query") String query);
=======
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
    List<Beneficiary> findByClientId(Long clientId);
    Optional<Beneficiary> findByRib(String rib); // Changed to Optional<Beneficiary>
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
}
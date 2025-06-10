package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> getUserRoleByEmail(String email);
    List<User> findByLastNameContains(String keyword);
    Optional<User>  findByUsername(String username);
    Optional<User> getUserRoleByUsername (String username);
    Optional<User> findByToken(String token);
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ma.ensa.ebankingver1.model.Role.CLIENT")
    long countClients();
    @Query("SELECT u FROM User u WHERE :service MEMBER OF u.servicesActifs")
    List<User> findUsersWithActiveService(@Param("service") String service);

    @Query("""
   SELECT u FROM User u
   LEFT JOIN FETCH u.accounts
   WHERE u.role = :role
   AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<User> findUsersWithAccountsByFullName(@Param("role") Role role, @Param("name") String name);

    // Recherche par nom utilisateur ou numéro de compte (join avec Account)
//    @Query("SELECT DISTINCT u FROM User u " +
//            "LEFT JOIN FETCH u.accounts a " +
//            "LEFT JOIN FETCH a.transactions t " +
//            "WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) " +
//            "OR LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
//    List<User> searchUsersWithAccountsAndTransactions(@Param("query") String query);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.role = :role")
    List<User> findUsersWithAccountsByRole(@Param("role") Role role);

    @Query("""
    SELECT u FROM User u 
    LEFT JOIN FETCH u.accounts 
    WHERE u.role = :role 
    AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) 
         OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
""")
    List<User> findUsersWithAccountsByRoleAndNameContaining(@Param("role") Role role, @Param("name") String name);
}
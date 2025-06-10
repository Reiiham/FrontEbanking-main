package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
<<<<<<< HEAD
=======

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    //List<BankAccount> findByClient(User client);
    //List<BankAccount> findByClientId(Long clientId);
    List<BankAccount> findByUser(User user);
    BankAccount findByRib(String rib);
    List<BankAccount> findByUserId(Long userId);
<<<<<<< HEAD
   /* @Query("SELECT ba FROM BankAccount ba WHERE ba.user.id = :userId AND ba.type = :type")*/
    List<BankAccount> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);


=======
    @Query("SELECT ba FROM BankAccount ba WHERE ba.user.id = :userId AND ba.type = :type")
    List<BankAccount> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
}



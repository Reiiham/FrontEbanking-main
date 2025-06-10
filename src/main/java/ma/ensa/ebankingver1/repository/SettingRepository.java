package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
<<<<<<< HEAD
=======

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
public interface SettingRepository extends JpaRepository<GlobalSetting, Long> {
    Optional<GlobalSetting> findByKey(String key);
}

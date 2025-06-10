package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface SettingRepository extends JpaRepository<GlobalSetting, Long> {
    Optional<GlobalSetting> findByKey(String key);
}

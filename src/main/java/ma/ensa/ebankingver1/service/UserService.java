package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.BankService;
import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public Role getUserRoleByEmail(String email) {
        return userRepository.getUserRoleByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Rôle non trouvé"))
                .getRole();
    }

    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }



    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }
    public Role getUserRoleByUsername(String username) {
        return userRepository.getUserRoleByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Rôle non trouvé"))
                .getRole();
    }
    public String getUserPhoneByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getTel();
    }
    public boolean hasActiveService(Long userId, BankService service) {
        User user = findById(userId);
        return user != null && user.getServicesActifs().contains(service.name());
    }

}
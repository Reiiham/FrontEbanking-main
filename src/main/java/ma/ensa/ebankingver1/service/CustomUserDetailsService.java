package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("customUserDetailsService")
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor injection (meilleure pratique)
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== Tentative de connexion pour username: " + username + " ===");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Utilisateur non trouvé: " + username);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'username : " + username);
                });

        System.out.println("✅ Utilisateur trouvé: " + user.getUsername());
        System.out.println("🔑 Mot de passe encodé en BD: " + user.getPassword());
        System.out.println("👤 Rôle: " + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, // isEnabled - compte activé
                true, // isAccountNonExpired - compte non expiré
                true, // isCredentialsNonExpired - credentials non expirés
                true, // isAccountNonLocked - compte non verrouillé
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

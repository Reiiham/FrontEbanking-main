package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
=======
import org.springframework.context.annotation.Primary;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("customUserDetailsService")
<<<<<<< HEAD
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'username : " + username));
=======
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
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
<<<<<<< HEAD
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

}
=======
                true, // isEnabled - compte activé
                true, // isAccountNonExpired - compte non expiré
                true, // isCredentialsNonExpired - credentials non expirés
                true, // isAccountNonLocked - compte non verrouillé
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

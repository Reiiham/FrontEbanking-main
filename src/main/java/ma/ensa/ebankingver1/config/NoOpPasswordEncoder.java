package ma.ensa.ebankingver1.config;
import org.springframework.security.crypto.password.PasswordEncoder;


//dummy class, juste pour que le projet compile
public class NoOpPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // ne fait rien, renvoie le mot de passe en clair
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // compare en clair
        return rawPassword.toString().equals(encodedPassword);
    }
}

package ma.ensa.ebankingver1.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
    }
}
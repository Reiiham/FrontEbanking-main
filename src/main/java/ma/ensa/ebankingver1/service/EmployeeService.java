package ma.ensa.ebankingver1.service;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import ma.ensa.ebankingver1.DTO.EmployeeDTO;
//import ma.ensa.ebankingver1.controller.ClientController;
import ma.ensa.ebankingver1.controller.AdminController;
import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailtrapClient mailtrapClient;


    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int USERNAME_DIGITS = 6;
    private static final int USERNAME_LETTERS = 2;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    // URL de base de votre application frontend
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public User createEmployee(EmployeeDTO employeeDTO) {
        String username = generateUniqueUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setUsername(username);
        employee.setEmail(employeeDTO.getEmailPersonnel());
        employee.setCin(employeeDTO.getCin());
        employee.setRole(Role.EMPLOYEE);

        // Générer un token sécurisé pour le changement de mot de passe
        String resetToken = generateSecureToken();
        employee.setToken(resetToken);
        employee.setTokenExpiry(LocalDateTime.now().plusHours(24)); // Token valide 24h

        // Pas de mot de passe initial - l'utilisateur devra le créer
        employee.setPassword(null);
        employee.setMustChangePassword(true);
        employee = userRepository.save(employee);

        // Créer le lien de changement de mot de passe
        String resetLink = frontendUrl + "/set-password?token=" + resetToken;

        // Envoyer l'email avec le lien
        String message = String.format("""
        Bonjour %s,
        
        Votre compte a été créé avec succès.
        
        Identifiant : %s
        
        Pour activer votre compte, veuillez cliquer sur le lien ci-dessous pour définir votre mot de passe :
        %s
        
        Ce lien est valide pendant 24 heures.
        
        Si vous n'arrivez pas à cliquer sur le lien, copiez-collez l'URL complète dans votre navigateur.
        
        Cordialement,
        L'équipe RH
        """, employee.getFirstName(), username, resetLink);

        sendEmail(employee.getEmail(), "Activation de votre compte", message);
        return employee;
    }

    public void sendEmail(String to, String subject, String content) {
        try {
            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address("hello@demomailtrap.com", "Équipe RH"))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .text(content)
                    .category("Employee Registration")
                    .build();

            System.out.println("Sending email response: " + mailtrapClient.send(mail));
            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    // Nouvelle méthode pour définir le mot de passe avec le token
    public void setPasswordWithToken(String token, String newPassword) {
        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou expiré"));

        // Vérifier si le token n'est pas expiré
        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        // Définir le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setTokenExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    // Méthode existante modifiée pour les changements de mot de passe normaux
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setTokenExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    // Générer un token sécurisé pour le reset de mot de passe
    private String generateSecureToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }

    private String generateUniqueUsername() {
        String username;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < USERNAME_LETTERS; i++) {
                sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            }
            for (int i = 0; i < USERNAME_DIGITS; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            username = sb.toString();
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }

    // Méthode pour renvoyer un lien d'activation si nécessaire
    public void resendActivationLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Générer un nouveau token
        String resetToken = generateSecureToken();
        user.setToken(resetToken);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Renvoyer l'email
        String resetLink = frontendUrl + "/set-password?token=" + resetToken;
        String message = String.format("""
        Bonjour %s,
        
        Voici votre nouveau lien d'activation de compte :
        %s
        
        Ce lien est valide pendant 24 heures.
        
        Cordialement,
        L'équipe RH
        """, user.getFirstName(), resetLink);

        sendEmail(user.getEmail(), "Nouveau lien d'activation", message);
    }

    // Méthode pour valider un token

    public boolean isTokenValid(String token) {
        logger.info("=== IS TOKEN VALID START ===");
        logger.info("Checking token: {}", token);

        try {
            if (token == null || token.trim().isEmpty()) {
                logger.error("Token is null or empty");
                return false;
            }

            logger.info("Searching for user with token...");
            Optional<User> userOpt = userRepository.findByToken(token);

            if (!userOpt.isPresent()) {
                logger.error("No user found with this token");
                return false;
            }

            User user = userOpt.get();
            logger.info("Found user: {} (ID: {})", user.getUsername(), user.getId());
            logger.info("User token expiry: {}", user.getTokenExpiry());
            logger.info("Current time: {}", LocalDateTime.now());

            if (user.getTokenExpiry() == null) {
                logger.error("Token expiry is null for user: {}", user.getUsername());
                return false;
            }

            boolean isValid = user.getTokenExpiry().isAfter(LocalDateTime.now());
            logger.info("Token is valid: {}", isValid);

            return isValid;

        } catch (Exception e) {
            logger.error("Exception in isTokenValid: ", e);
            return false;
        } finally {
            logger.info("=== IS TOKEN VALID END ===");
        }
    }
}
/*
@Service
@Transactional
public class EmployeeService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailtrapClient mailtrapClient; // Changed from JavaMailSender

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int USERNAME_DIGITS = 6;
    private static final int USERNAME_LETTERS = 2;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    public User createEmployee(EmployeeDTO employeeDTO) {
        String username = generateUniqueUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setUsername(username);
        employee.setEmail(employeeDTO.getEmailPersonnel());
        employee.setCin(employeeDTO.getCin());
        //employee.setCinPhotoFilename(employeeDTO.getCinPhotoFilename()); // Stocker le nom du fichier
        employee.setRole(Role.EMPLOYEE);

        String tempPassword = generateRandomPassword();
        employee.setPassword(passwordEncoder.encode(tempPassword));
        employee.setMustChangePassword(true);
        employee = userRepository.save(employee);

        // Send email with Mailtrap API
        String message = String.format("""
        Bonjour %s,
        
        Votre compte a été créé avec succès.
        
        Identifiant : %s
        Mot de passe temporaire : %s
        
        Veuillez vous connecter et modifier votre mot de passe dès que possible.
        
        Cordialement,
        L'équipe RH
        """, employee.getFirstName(), username, tempPassword);

        sendEmail(employee.getEmail(), "Bienvenue sur la plateforme", message);
        return employee;
    }

    public void sendEmail(String to, String subject, String content) {
        try {
            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address("hello@demomailtrap.com", "Équipe RH"))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .text(content)
                    .category("Employee Registration")
                    .build();

            System.out.println("Sending email response: " + mailtrapClient.send(mail));
            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        if (!user.getMustChangePassword()) {
            throw new IllegalStateException("Le changement de mot de passe n'est pas requis");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private String generateUniqueUsername() {
        String username;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < USERNAME_LETTERS; i++) {
                sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            }
            for (int i = 0; i < USERNAME_DIGITS; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            username = sb.toString();
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }
}

 */

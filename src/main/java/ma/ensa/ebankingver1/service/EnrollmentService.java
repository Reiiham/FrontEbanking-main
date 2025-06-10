package ma.ensa.ebankingver1.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.*;
import ma.ensa.ebankingver1.repository.*;
import ma.ensa.ebankingver1.util.EncryptionUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Random;
@Service
public class EnrollmentService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MailtrapClient mailtrapClient; // Ajout du client email

    public EnrollmentService(UserRepository userRepository,
                             AccountRepository accountRepository,
                             TransactionRepository transactionRepository,
                             MailtrapClient mailtrapClient,
                             PasswordEncoder passwordEncoder) { // Injection via constructeur
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.mailtrapClient = mailtrapClient;
        this.passwordEncoder = passwordEncoder; // Initialisation du PasswordEncoder
    }

    public long countClients() {
        return userRepository.countClients();
    }

    public long countAccounts() {
        return accountRepository.count();
    }

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int USERNAME_LETTERS = 3;
    private static final int USERNAME_DIGITS = 4;
    private static final Random RANDOM = new Random();

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

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private static final String ACCOUNT_PREFIX = "ACC";
    private static final String SUPERVISOR_CODE = "supervisor";

    public boolean validateSupervisor(String code) {
        return SUPERVISOR_CODE.equals(code);
    }

    @Transactional
    public void enrollClient(EnrollmentRequest dto) {
        // Création de l'utilisateur
        User client = new User();
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setTel(dto.getTel());
        client.setBirth_Date(LocalDate.parse(dto.getBirthDate()));
        client.setRole(Role.CLIENT);
        client.setCin(dto.getCin());

        // Username et password auto-générés
        String generatedUsername = generateUniqueUsername();
        String generatedPassword = generateRandomPassword(8);

        client.setUsername(generatedUsername);
        client.setPassword(passwordEncoder.encode(generatedPassword));
        userRepository.save(client);

        // Génération du numéro lisible et chiffrement
        String rawAccNum = generateReadableAccountNumber();
        String encryptedAccNum = EncryptionUtil.encrypt(rawAccNum);

        // Création du compte
        BankAccount account = new BankAccount();
        account.setRawAccountNumber(rawAccNum);
        account.setAccountNumber(encryptedAccNum);
        account.setRib(generateRib());
        account.setType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setUser(client);

        accountRepository.save(account);

        // Envoi de l'email avec les identifiants
        String message = String.format("""
        Bonjour %s,
        
        Votre compte bancaire a été créé avec succès.
        
        Vos identifiants de connexion :
        Nom d'utilisateur : %s
        Mot de passe : %s
        Numéro de compte : %s
        RIB : %s
        
        Veuillez conserver ces informations en lieu sûr et vous connecter dès que possible.
        
        Cordialement,
        L'équipe bancaire
        """, client.getFirstName(), generatedUsername, generatedPassword, rawAccNum, account.getRib());

        sendEmail(client.getEmail(), "Bienvenue - Vos identifiants bancaires", message);

        System.out.println("Identifiants créés et envoyés par email → username: " + generatedUsername + ", password: " + generatedPassword);
    }

    // Méthode pour envoyer l'email (similaire à EmployeeService)
    public void sendEmail(String to, String subject, String content) {
        try {
            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address("hello@demomailtrap.com", "Équipe Bancaire"))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .text(content)
                    .category("Client Registration")
                    .build();

            System.out.println("Sending email response: " + mailtrapClient.send(mail));
            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    // Générateur de numéro de compte au format ACCXXXXXXX
    private String generateReadableAccountNumber() {
        int number = new Random().nextInt(9000000) + 1000000;
        return "ACC" + number;
    }

    private String generateRib() {
        String bankCode = "12345";
        String branchCode = "67890";
        String accountNumberPart = String.format("%011d", (long)(Math.random() * 1_000_000_000L));
        String key = String.format("%02d", (int)(Math.random() * 100));
        return bankCode + branchCode + accountNumberPart + key;
    }

    @Transactional
    public boolean updateClient(ClientUpdateRequest dto) {
        Optional<User> opt = userRepository.findById(dto.getClientId());
        if (opt.isEmpty()) return false;

        User client = opt.get();

        if (dto.getNewFirstName() != null) client.setFirstName(dto.getNewFirstName());
        if (dto.getNewLastName() != null) client.setLastName(dto.getNewLastName());
        if (dto.getNewEmail() != null) client.setEmail(dto.getNewEmail());
        if (dto.getNewTel() != null) client.setTel(dto.getNewTel());

        userRepository.save(client);
        return true;
    }

    @Transactional
    public boolean deleteClient(Long clientId) {
        Optional<User> opt = userRepository.findById(clientId);
        if (opt.isEmpty()) return false;

        if (!transactionRepository.findByUserId(clientId).isEmpty()) return false;

        User client = opt.get();
        accountRepository.deleteByUserId(clientId);
        userRepository.delete(client);
        return true;
    }
}

/*
@Service
public class EnrollmentService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public EnrollmentService(UserRepository userRepository,
                             AccountRepository accountRepository,
                             TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public long countClients() {
        return userRepository.countClients();
    }

    public long countAccounts() {
        return accountRepository.count();
    }

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int USERNAME_LETTERS = 3;
    private static final int USERNAME_DIGITS = 4;
    private static final Random RANDOM = new Random();

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
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }



    private static final String ACCOUNT_PREFIX = "ACC";

    private static final String SUPERVISOR_CODE = "supervisor";

    public boolean validateSupervisor(String code) {
        return SUPERVISOR_CODE.equals(code);
    }

    @Transactional
    public void enrollClient(EnrollmentRequest dto) {
        // Création de l'utilisateur
        User client = new User();
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setTel(dto.getTel());
        client.setBirth_Date(LocalDate.parse(dto.getBirthDate()));
        client.setRole(Role.CLIENT);
        client.setCin(dto.getCin());
        // Username auto-généré
        String generatedUsername = generateUniqueUsername();
        String generatedPassword = generateRandomPassword(8);

        client.setUsername(generatedUsername);
        client.setPassword(generatedPassword);

        userRepository.save(client);

        // Génération du numéro lisible et chiffrement
        String rawAccNum = generateReadableAccountNumber();
        String encryptedAccNum = EncryptionUtil.encrypt(rawAccNum);

        // Création du compte
        BankAccount account = new BankAccount();
        account.setRawAccountNumber(rawAccNum);
        account.setAccountNumber(encryptedAccNum); // champ chiffré
        account.setRib(generateRib()); // nouveau champ RIB généré
        account.setType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setUser(client);

        accountRepository.save(account);

        System.out.println("Identifiants créés → username: " + generatedUsername + ", password: " + generatedPassword);

    }

    // Générateur de numéro de compte au format ACCXXXXXXX
    private String generateReadableAccountNumber() {
        int number = new Random().nextInt(9000000) + 1000000; // garantit 7 chiffres
        return "ACC" + number;
    }

    private String generateRib() {
        String bankCode = "12345";
        String branchCode = "67890";
        String accountNumberPart = String.format("%011d", (long)(Math.random() * 1_000_000_000L));
        String key = String.format("%02d", (int)(Math.random() * 100));
        return bankCode + branchCode + accountNumberPart + key;
    }

    @Transactional
    public boolean updateClient(ClientUpdateRequest dto) {
        Optional<User> opt = userRepository.findById(dto.getClientId());
        if (opt.isEmpty()) return false;

        User client = opt.get();

        if (dto.getNewFirstName() != null) client.setFirstName(dto.getNewFirstName());
        if (dto.getNewLastName() != null) client.setLastName(dto.getNewLastName());
        if (dto.getNewEmail() != null) client.setEmail(dto.getNewEmail());
        if (dto.getNewTel() != null) client.setTel(dto.getNewTel());

        userRepository.save(client);
        return true;
    }


    @Transactional
    public boolean deleteClient(Long clientId) {
        Optional<User> opt = userRepository.findById(clientId);
        if (opt.isEmpty()) return false;

        if (!transactionRepository.findByUserId(clientId).isEmpty()) return false;

        User client = opt.get();
        accountRepository.deleteByUserId(clientId); // méthode custom ou native query
        userRepository.delete(client);
        return true;
    }
}

 */

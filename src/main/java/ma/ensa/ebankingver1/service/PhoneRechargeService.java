package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.PhoneRechargeRequest;
import ma.ensa.ebankingver1.DTO.PhoneRechargeResponse;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.PhoneRecharge;
import ma.ensa.ebankingver1.model.RechargeStatus;
import ma.ensa.ebankingver1.repository.PhoneRechargeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PhoneRechargeService {

    private static final Logger logger = LoggerFactory.getLogger(PhoneRechargeService.class);

    @Autowired
    private PhoneRechargeRepository rechargeRepository;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private OperatorApiService operatorApiService;

    @Autowired
    private UserService userService;


    public PhoneRechargeResponse processRecharge(PhoneRechargeRequest request, String username) {
        logger.info("Processing recharge for user: {} to phone: {}", username, request.getPhoneNumber());

        PhoneRecharge recharge = null;

        try {
            // 🔁 1. Récupérer l'ID utilisateur
            Long userId = userService.getUserIdByUsername(username);

            // 🔁 2. Récupérer un compte bancaire associé à l'utilisateur
            List<BankAccount> accounts = bankAccountService.findByUserId(userId);
            if (accounts.isEmpty()) {
                throw new RuntimeException("Aucun compte bancaire trouvé pour l'utilisateur " + username);
            }
            String rib = accounts.get(0).getRib(); // ✅ Le vrai RIB

            // ✅ 3. Validation
            validateRechargeRequest(request, rib);

            // 4. Créer l'enregistrement
            recharge = createRechargeRecord(request, rib);

            logger.info("Recharge record created with reference: {}", recharge.getTransactionReference());

            // 5. Débiter
            bankAccountService.debitAccountByRib(rib, request.getAmount(), "RECHARGE_" + recharge.getTransactionReference());

            // 6. Traitement opérateur
            recharge.setStatus(RechargeStatus.PROCESSING);
            rechargeRepository.save(recharge);

            boolean operatorSuccess = operatorApiService.processRecharge(
                    request.getOperatorCode(),
                    request.getPhoneNumber(),
                    request.getAmount()
            );

            if (operatorSuccess) {
                recharge.setStatus(RechargeStatus.COMPLETED);
                recharge.setProcessedAt(LocalDateTime.now());
                rechargeRepository.save(recharge);

                return new PhoneRechargeResponse(
                        recharge.getTransactionReference(),
                        request.getAmount(),
                        request.getPhoneNumber()
                );
            } else {
                handleRechargeFailure(recharge, rib, request.getAmount());
                return new PhoneRechargeResponse("Échec de la recharge chez l'opérateur");
            }

        } catch (RuntimeException e) {
            logger.error("Recharge failed for user {}: {}", username, e.getMessage());
            if (recharge != null) {
                recharge.setStatus(RechargeStatus.FAILED);
                recharge.setFailureReason(e.getMessage());
                recharge.setProcessedAt(LocalDateTime.now());
                rechargeRepository.save(recharge);
            }
            return new PhoneRechargeResponse("Erreur: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during recharge: {}", e.getMessage(), e);
            if (recharge != null) {
                recharge.setStatus(RechargeStatus.FAILED);
                recharge.setFailureReason("Erreur système");
                recharge.setProcessedAt(LocalDateTime.now());
                rechargeRepository.save(recharge);
            }
            return new PhoneRechargeResponse("Erreur système. Veuillez réessayer.");
        }
    }


    private void validateRechargeRequest(PhoneRechargeRequest request, String accountNumber) {
        logger.info("Validating recharge request for account: {}", accountNumber);

        // Vérifier le solde du compte (utiliser le RIB)
        BigDecimal accountBalance = bankAccountService.getAccountBalanceByRib(accountNumber);
        if (accountBalance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Solde insuffisant. Solde actuel: " + accountBalance + " DH");
        }

        // Vérifier le PIN de transaction (utiliser le RIB)
        if (!bankAccountService.validateTransactionPinByRib(accountNumber, request.getTransactionPin())) {
            throw new RuntimeException("PIN de transaction incorrect");
        }

        // Vérifier les limites quotidiennes
        BigDecimal dailyRecharges = getDailyRechargeAmount(accountNumber);
        BigDecimal dailyLimit = new BigDecimal("1000");

        if (dailyRecharges.add(request.getAmount()).compareTo(dailyLimit) > 0) {
            throw new RuntimeException("Limite quotidienne de recharge dépassée. " +
                    "Utilisé aujourd'hui: " + dailyRecharges + " DH");
        }

        logger.info("Validation completed successfully for account: {}", accountNumber);
    }

    private PhoneRecharge createRechargeRecord(PhoneRechargeRequest request,
                                               String clientAccountNumber) {
        PhoneRecharge recharge = new PhoneRecharge();
        recharge.setPhoneNumber(request.getPhoneNumber());
        recharge.setOperatorCode(request.getOperatorCode());
        recharge.setAmount(request.getAmount());
        recharge.setClientAccountNumber(clientAccountNumber);
        recharge.setStatus(RechargeStatus.PENDING);

        return rechargeRepository.save(recharge);
    }
    public List<PhoneRecharge> getRechargeHistoryByUsername(String username) {
        Long userId = userService.getUserIdByUsername(username);
        List<BankAccount> accounts = bankAccountService.findByUserId(userId);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No bank accounts found for user: " + username);
        }
        return getRechargeHistory(accounts.get(0).getRib());
    }
    private void handleRechargeFailure(PhoneRecharge recharge, String accountNumber,
                                       BigDecimal amount) {
        recharge.setStatus(RechargeStatus.FAILED);
        recharge.setFailureReason("Échec API opérateur");
        recharge.setProcessedAt(LocalDateTime.now());
        rechargeRepository.save(recharge);

        // Recréditer le compte (utiliser le RIB)
        bankAccountService.creditAccountByRib(accountNumber, amount,
                "REFUND_" + recharge.getTransactionReference());
    }

    private BigDecimal getDailyRechargeAmount(String accountNumber) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<PhoneRecharge> dailyRecharges = rechargeRepository.findRechargeHistory(
                accountNumber, startOfDay, endOfDay
        );

        return dailyRecharges.stream()
                .filter(r -> r.getStatus() == RechargeStatus.COMPLETED)
                .map(PhoneRecharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<PhoneRecharge> getRechargeHistory(String accountNumber) {
        return rechargeRepository.findByClientAccountNumberOrderByCreatedAtDesc(accountNumber);
    }
}
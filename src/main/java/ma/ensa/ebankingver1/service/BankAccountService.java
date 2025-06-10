package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;
<<<<<<< HEAD
import ma.ensa.ebankingver1.model.User;

import java.math.BigDecimal;
=======

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import java.util.List;

public interface BankAccountService {

<<<<<<< HEAD
    // Méthodes existantes (déjà présentes)
=======
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
    BankAccount findById(String id);
    BankAccount findByRib(String rib);
    BankAccount save(BankAccount account);
    List<BankAccount> findByUserId(Long userId);

<<<<<<< HEAD
    // Méthodes pour le crypto trading
    boolean debitUser(User user, double amount, String reason);
    boolean creditUser(User user, double amount, String reason);
    // Nouvelles méthodes avec RIB (numéro de compte)
    BigDecimal getAccountBalanceByRib(String rib);
    boolean validateTransactionPinByRib(String rib, String transactionPin);
    void debitAccountByRib(String rib, BigDecimal amount, String description);
    void creditAccountByRib(String rib, BigDecimal amount, String description);

}
=======
    /*
    ClientDTO saveClient(ClientDTO clientDTO);

    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, Long clientId, double overDraft);
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, Long clientId, double interestRate);
    List<ClientDTO> listClients();
    BankAccountDTO getBankAccount(String accountId);
    void debit(String accountId, double amount, String description);
    void credit(String accountId, double amount, String description);
    void transfer(String accountIdSource, String accountIdDestination, double amount); // Fixed typo
    List<BankAccountDTO> bankAccountsList();
    ClientDTO getClient(Long clientId) throws Exception;
    ClientDTO updateClient(ClientDTO clientDTO);
    void deleteClient(Long clientId);
    List<AccountOperationDTO> accountHistory(String accountId); // Fixed method name
    AccountHistoryDTO getAccountHistory(String accountId, int page, int size);
    List<ClientDTO> searchClients(String keyword);
    List<BankAccount> getaccountsClient(Long clientId);
    List<BankAccountDTO> getAccountsByClientId(Long clientId);
    List<BeneficiaryDTO> getBeneficiaries(String clientId); // Added
    BeneficiaryDTO addBeneficiary(String clientId, BeneficiaryDTO beneficiaryDTO);
    public List<BankAccountDTO> getAllBankAccounts();// Added

     */
}

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

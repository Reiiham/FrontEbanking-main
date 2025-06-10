package ma.ensa.ebankingver1.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.*;
//import ma.ensa.ebankingver1.repository.AccountOperationRepository;
//import ma.ensa.ebankingver1.repository.BankAccountRepository;
import ma.ensa.ebankingver1.repository.BankAccountRepository;
import ma.ensa.ebankingver1.repository.BeneficiaryRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public BankAccount findById(String id) {
        return bankAccountRepository.findById(id).orElse(null);
    }

    @Override
    public BankAccount findByRib(String rib) {
        return bankAccountRepository.findByRib(rib);
    }

    @Override
    public BankAccount save(BankAccount account) {
        return bankAccountRepository.save(account);
    }

    @Override
    public List<BankAccount> findByUserId(Long userId) {
        return bankAccountRepository.findByUserId(userId);
    }
    /*
    @Autowired
    private UserRepository clientRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private AccountOperationRepository accountOperationRepository;
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    @Autowired
    private BankAccountMapperImpl dtoMapper;

    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    @Override
    public ClientDTO saveClient(ClientDTO clientDTO) {
        log.info("Saving new Client");
        User client = dtoMapper.fromClientDTO(clientDTO);
        User savedClient = clientRepository.save(client);
        return dtoMapper.fromClient(savedClient);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, Long clientId, double overDraft) {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreateAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setClient(client);
        currentAccount.setOverDraft(overDraft);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, Long clientId, double interestRate) {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreateAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setClient(client);
        savingAccount.setInterestRate(interestRate);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public List<ClientDTO> listClients() {
        List<User> clients = clientRepository.findAll();
        return clients.stream().map(dtoMapper::fromClient).collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> bankAccountsList() {
        log.info("Fetching all bank accounts");
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return accounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void debit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        if (bankAccount.getBalance() < amount) throw new RuntimeException("Balance not sufficient");
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setOperationDate(new Date());
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }
    @Override
    public void credit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setOperationDate(new Date());
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void transfer(String accountSource, String accountDestination, double amount) {
        log.info("Initiating transfer: from {} to {}, amount: {}", accountSource, accountDestination, amount);

        if (amount <= 0) {
            log.warn("Transfer rejected: amount {} is not positive", amount);
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        BankAccount sourceAccount = bankAccountRepository.findById(accountSource)
                .orElseThrow(() -> {
                    log.error("Source account not found: {}", accountSource);
                    return new EntityNotFoundException("Source account not found: " + accountSource);
                });

        BankAccount destinationAccount;
        Optional<BankAccount> destinationById = bankAccountRepository.findById(accountDestination);
        if (destinationById.isPresent()) {
            destinationAccount = destinationById.get();
            log.debug("Destination found as direct account: {}", accountDestination);
        } else {
            Optional<Beneficiary> beneficiaryOpt = beneficiaryRepository.findByRib(accountDestination);
            if (beneficiaryOpt.isPresent()) {
                Beneficiary beneficiary = beneficiaryOpt.get();
                destinationAccount = beneficiary.getAccount();
                if (destinationAccount == null) {
                    log.error("Destination account not found for RIB: {}", accountDestination);
                    throw new EntityNotFoundException("Destination account not found for RIB: " + accountDestination);
                }
                log.debug("Destination resolved via beneficiary RIB: {}", accountDestination);
            } else {
                log.error("No account or beneficiary found for: {}", accountDestination);
                throw new EntityNotFoundException("No account or beneficiary found for: " + accountDestination);
            }
        }

        if (sourceAccount.getBalance() < amount) {
            log.warn("Transfer rejected: insufficient balance in source account {}, balance: {}", accountSource, sourceAccount.getBalance());
            throw new IllegalStateException("Insufficient balance in source account");
        }

        double newSourceBalance = sourceAccount.getBalance() - amount;
        double newDestinationBalance = destinationAccount.getBalance() + amount;
        sourceAccount.setBalance(newSourceBalance);
        destinationAccount.setBalance(newDestinationBalance);
        log.info("Transfer executed: from {} (new balance: {}) to {} (new balance: {})", accountSource, newSourceBalance, accountDestination, newDestinationBalance);

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        AccountOperation debitOperation = new AccountOperation();
        debitOperation.setOperationDate(new Date());
        debitOperation.setType(OperationType.DEBIT);
        debitOperation.setAmount(amount);
        debitOperation.setDescription("Transfer to " + accountDestination);
        debitOperation.setBankAccount(sourceAccount);
        accountOperationRepository.save(debitOperation);

        AccountOperation creditOperation = new AccountOperation();
        creditOperation.setOperationDate(new Date());
        creditOperation.setType(OperationType.CREDIT);
        creditOperation.setAmount(amount);
        creditOperation.setDescription("Transfer from " + accountSource);
        creditOperation.setBankAccount(destinationAccount);
        accountOperationRepository.save(creditOperation);

        log.info("Transfer completed successfully: from {} to {}, amount: {}", accountSource, accountDestination, amount);
    }
    @Override
    public BankAccountDTO getBankAccount(String accountId) {
        log.info("Fetching bank account with ID: {}", accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        if (bankAccount instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        }
    }

    @Override
    public ClientDTO getClient(Long clientId) throws Exception {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new Exception("Client not found"));
        return dtoMapper.fromClient(client);
    }

    @Override
    public ClientDTO updateClient(ClientDTO clientDTO) {
        log.info("Updating client");
        User client = dtoMapper.fromClientDTO(clientDTO);
        User savedClient = clientRepository.save(client);
        return dtoMapper.fromClient(savedClient);
    }

    @Override
    public void deleteClient(Long clientId) {
        clientRepository.deleteById(clientId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId);
        return accountOperations.stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        Page<AccountOperation> operations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        accountHistoryDTO.setAccountOperationDTOS(operations.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList()));
        accountHistoryDTO.setAccountId(bankAccount != null ? bankAccount.getId() : "");
        accountHistoryDTO.setBalance(bankAccount != null ? bankAccount.getBalance() : 0.0);
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(operations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<ClientDTO> searchClients(String keyword) {
        List<User> clients = clientRepository.findByLastNameContains(keyword);
        return clients.stream().map(dtoMapper::fromClient).collect(Collectors.toList());
    }

    @Override
    public List<BankAccount> getaccountsClient(Long clientId) {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return bankAccountRepository.findByClient(client);
    }

    @Override
    public List<BankAccountDTO> getAccountsByClientId(Long clientId) {
        List<BankAccount> accounts = bankAccountRepository.findByClientId(clientId);
        return accounts.stream()
                .map(account -> {
                    if (account instanceof CurrentAccount) {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    } else {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    }
                })
                .toList();
    }

    @Override
    public List<BeneficiaryDTO> getBeneficiaries(String clientId) {
        Long id = Long.parseLong(clientId);
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByClientId(id);
        return beneficiaries.stream()
                .map(beneficiary -> {
                    BeneficiaryDTO dto = new BeneficiaryDTO();
                    dto.setId(beneficiary.getId());
                    dto.setRib(beneficiary.getRib());
                    dto.setName(beneficiary.getName());
                    if (beneficiary.getAccount() != null) {
                        dto.setAccountId(beneficiary.getAccount().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public BeneficiaryDTO addBeneficiary(String clientId, BeneficiaryDTO beneficiaryDTO) {
        Long id = Long.parseLong(clientId);
        User client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(UUID.randomUUID().toString());
        beneficiary.setClient(client);
        beneficiary.setRib(beneficiaryDTO.getRib());
        beneficiary.setName(beneficiaryDTO.getName());

        // Vérifier et associer le compte bancaire
        if (beneficiaryDTO.getAccountId() == null || beneficiaryDTO.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID is required for beneficiary");
        }
        BankAccount account = bankAccountRepository.findById(beneficiaryDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + beneficiaryDTO.getAccountId()));
        beneficiary.setAccount(account);

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);

        BeneficiaryDTO result = new BeneficiaryDTO();
        result.setId(savedBeneficiary.getId());
        result.setRib(savedBeneficiary.getRib());
        result.setName(savedBeneficiary.getName());
        result.setAccountId(savedBeneficiary.getAccount() != null ? savedBeneficiary.getAccount().getId() : null);
        return result;
    }
    public List<BankAccountDTO> getAllBankAccounts() {
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return accounts.stream()
                .map(account -> {
                    if (account instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    }
                })
                .collect(Collectors.toList());
    }

*/
}

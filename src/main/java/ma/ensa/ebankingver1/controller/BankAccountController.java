package ma.ensa.ebankingver1.controller;
/*
import ma.ensa.ebankingver1.DTO.AccountOperationDTO;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bankAccounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private static final Logger logger = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private AuditService auditService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccountDTO> getBankAccount(@PathVariable(name = "id") String accountId) {
        BankAccountDTO account = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountDTO>> listAccounts() {
        List<BankAccountDTO> accounts = bankAccountService.bankAccountsList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{accountId}/operations")
    public ResponseEntity<List<AccountOperationDTO>> getHistory(@PathVariable("accountId") String accountId) {
        List<AccountOperationDTO> operations = bankAccountService.accountHistory(accountId);
        return ResponseEntity.ok(operations);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    public ResponseEntity<AccountHistoryDTO> getAccountHistory(@PathVariable("accountId") String accountId,
                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                               @RequestParam(name = "size", defaultValue = "5") int size) {
        AccountHistoryDTO history = bankAccountService.getAccountHistory(accountId, page, size);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/accounts/debit")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DebitDTO> debit(@RequestBody DebitDTO debitDTO) {
        try {
            bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
            auditService.logAction("DEBIT_REQUEST", "BANK_ACCOUNT", debitDTO.getAccountId(),
                    Map.of("amount", debitDTO.getAmount(), "description", debitDTO.getDescription()), true);
            return ResponseEntity.ok(debitDTO);
        } catch (Exception e) {
            auditService.logAction("DEBIT_REQUEST_FAILED", "BANK_ACCOUNT", debitDTO.getAccountId(),
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(debitDTO);
        }
    }

    @PostMapping("/accounts/credit")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CreditDTO> credit(@RequestBody CreditDTO creditDTO) {
        try {
            bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription());
            auditService.logAction("CREDIT_REQUEST", "BANK_ACCOUNT", creditDTO.getAccountId(),
                    Map.of("amount", creditDTO.getAmount(), "description", creditDTO.getDescription()), true);
            return ResponseEntity.ok(creditDTO);
        } catch (Exception e) {
            auditService.logAction("CREDIT_REQUEST_FAILED", "BANK_ACCOUNT", creditDTO.getAccountId(),
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(creditDTO);
        }
    }

    @GetMapping("/accounts/customer/{customerId}")
    public ResponseEntity<List<BankAccount>> getAccountsCustomer(@PathVariable("customerId") Long customerId) {
        List<BankAccount> accounts = bankAccountService.getaccountsClient(customerId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/accounts/transfert")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> transfert(@RequestBody TransferRequestDTO transferRequestDTO) {
        try {
            bankAccountService.transfer(transferRequestDTO.getAccountSource(), transferRequestDTO.getAccountDestination(), transferRequestDTO.getAmount());
            auditService.logAction("TRANSFER_REQUEST", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("amount", transferRequestDTO.getAmount(), "destination", transferRequestDTO.getAccountDestination()), true);
            return ResponseEntity.ok("Transfer successful");
        } catch (Exception e) {
            auditService.logAction("TRANSFER_REQUEST_FAILED", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/test/audit")
    public ResponseEntity<String> testAudit() {
        auditService.testInsert();
        return ResponseEntity.ok("Audit test triggered");
    }
}
/*
@RestController
@RequestMapping("/api/client")
public class BankAccountController {
    private BankAccountService bankAccountService;
    public BankAccountController (BankAccountService bankAccountService){
        this.bankAccountService = bankAccountService;
    }

    @GetMapping(path = "/accounts/{id}")
    public BankAccountDTO getBankAccount(@PathVariable(name = "id") String accountId){
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping(path = "/accounts")
    public List<BankAccountDTO> listAccounts(){
        return bankAccountService.bankAccountsList();
    }

    @GetMapping("/accounts/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable("accountId") String accountId){
        return bankAccountService.AccountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistory(@PathVariable("accountId") String accountId,
                                               @RequestParam(name = "page",defaultValue = "0") int page,
                                               @RequestParam(name = "size",defaultValue = "5") int size){
        return bankAccountService.getAccountHistory(accountId,page,size);
    }

    @PostMapping(path = "/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO){
        this.bankAccountService.debit(debitDTO.getAccountId(),debitDTO.getAmount(),debitDTO.getDescritpion());
        return debitDTO;
    }

    @PostMapping(path = "/accounts/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO){
        this.bankAccountService.credit(creditDTO.getAccountId(),creditDTO.getAmount(),creditDTO.getDescritpion());
        return creditDTO;
    }

    @GetMapping(path = "/accounts/customer/{customerid}")
    public List<BankAccount> getaccountsCustomer(@PathVariable("Customerid") Long Customerid){
        return this.bankAccountService.getaccountsClient(Customerid);
    }


    @PostMapping(path = "/accounts/transfert")
    public void transfert(@RequestBody TransfertRequestDTO transfertRequestDTO){
        this.bankAccountService.transfert(transfertRequestDTO.getAccountSource(),transfertRequestDTO.getAccountDestination(),transfertRequestDTO.getAmount());
    }
}

 */





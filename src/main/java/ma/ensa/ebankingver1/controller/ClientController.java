
package ma.ensa.ebankingver1.controller;

import ma.ensa.ebankingver1.DTO.TransactionHistoryDTO;
import ma.ensa.ebankingver1.DTO.TransferRequest;
import ma.ensa.ebankingver1.DTO.TransferResponse;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.BankService;
import ma.ensa.ebankingver1.model.Transaction;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import ma.ensa.ebankingver1.service.BankAccountService;
import ma.ensa.ebankingver1.service.TransactionService;
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;
    /**
     * Effectuer un virement entre comptes
     */
    @PostMapping("/{clientId}/services/virement")
    public ResponseEntity<?> executeTransfer(
            @PathVariable("clientId") Long clientId,
            @RequestBody TransferRequest transferRequest,
            Authentication authentication) {

        try {
            // Vérifier que l'utilisateur connecté correspond au client
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            // DEBUG: Afficher les services actifs
            System.out.println("Services actifs du client: " + currentUser.getServicesActifs());

            // Vérifier que le service VIREMENT est actif pour ce client (vérification flexible)
            List<String> servicesActifs = currentUser.getServicesActifs();
            boolean virementActif = servicesActifs != null && (
                    servicesActifs.contains("VIREMENT") ||
                            servicesActifs.contains("VIREMENTS") ||  // Ajout du pluriel
                            servicesActifs.contains("virement") ||
                            servicesActifs.contains("virements") ||   // Ajout du pluriel en minuscule
                            servicesActifs.contains("Virement") ||
                            servicesActifs.contains("Virements") ||   // Ajout du pluriel avec majuscule
                            servicesActifs.stream().anyMatch(service ->
                                    service != null && (service.toUpperCase().equals("VIREMENT") ||
                                            service.toUpperCase().equals("VIREMENTS")))
            );

            if (!virementActif) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Service VIREMENT non activé pour ce client. Services actifs: " + servicesActifs));
            }

            // Vérifier que le compte source appartient au client
            BankAccount sourceAccount = bankAccountService.findById(transferRequest.getFromAccountId());
            if (sourceAccount == null || !sourceAccount.getUser().getId().equals(clientId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Compte source invalide"));
            }

            // Vérifier le solde suffisant
            if (sourceAccount.getBalance() < transferRequest.getAmount()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solde insuffisant"));
            }

            // Vérifier le montant positif
            if (transferRequest.getAmount() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le montant doit être positif"));
            }

            // Trouver le compte destinataire par RIB
            BankAccount destinationAccount = bankAccountService.findByRib(transferRequest.getToRib());
            if (destinationAccount == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Compte destinataire introuvable"));
            }

            // Effectuer le virement
            String transactionId = performTransfer(sourceAccount, destinationAccount,
                    transferRequest.getAmount(), transferRequest.getDescription(),
                    transferRequest.getBeneficiaryName(), currentUser);

            return ResponseEntity.ok(new TransferResponse(
                    true,
                    "Virement effectué avec succès",
                    transactionId,
                    sourceAccount.getBalance()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du virement: " + e.getMessage()));
        }
    }

    /**
     * Obtenir l'historique des virements d'un client
     */
    @GetMapping("/{clientId}/services/virement/history")
    public ResponseEntity<?> getTransferHistory(
            @PathVariable("clientId") Long clientId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            List<Transaction> transfers = transactionService.findTransfersByUserId(clientId, page, size);

            // Convertir en DTO pour éviter la sérialisation circulaire
            List<TransactionHistoryDTO> transferHistory = transfers.stream()
                    .map(TransactionHistoryDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(transferHistory);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'historique"));
        }
    }

    /**
     * Valider un RIB destinataire
     */
    @PostMapping("/{clientId}/services/virement/validate-rib")
    public ResponseEntity<?> validateRib(
            @PathVariable("clientId") Long clientId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            String rib = request.get("rib");
            BankAccount account = bankAccountService.findByRib(rib);

            if (account != null) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "accountHolder", account.getUser().getFirstName() + " " + account.getUser().getLastName(),
                        "accountType", account.getType()
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la validation du RIB"));
        }
    }

    /**
     * Obtenir les comptes du client pour les virements
     */
    @GetMapping("/{clientId}/services/virement/accounts")
    public ResponseEntity<?> getClientAccountsForTransfer(
            @PathVariable("clientId") Long clientId,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            List<BankAccount> accounts = currentUser.getAccounts();
            return ResponseEntity.ok(accounts);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des comptes"));
        }
    }

    /**
     * Méthode privée pour effectuer le virement
     */
    private String performTransfer(BankAccount sourceAccount, BankAccount destinationAccount,
                                   double amount, String description, String beneficiaryName, User user) {

        // Générer un ID de transaction unique
        String transactionId = UUID.randomUUID().toString();

        // Débiter le compte source
        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        bankAccountService.save(sourceAccount);

        // Créditer le compte destination
        destinationAccount.setBalance(destinationAccount.getBalance() + amount);
        bankAccountService.save(destinationAccount);

        // Créer la transaction de débit
        Transaction debitTransaction = new Transaction();
        debitTransaction.setId(transactionId + "_DEBIT");
        debitTransaction.setType("VIREMENT_SORTANT");
        debitTransaction.setAmount(-amount);
        debitTransaction.setDate(LocalDateTime.now());
        debitTransaction.setAccount(sourceAccount);
        debitTransaction.setUser(user);
        transactionService.save(debitTransaction);

        // Créer la transaction de crédit
        Transaction creditTransaction = new Transaction();
        creditTransaction.setId(transactionId + "_CREDIT");
        creditTransaction.setType("VIREMENT_ENTRANT");
        creditTransaction.setAmount(amount);
        creditTransaction.setDate(LocalDateTime.now());
        creditTransaction.setAccount(destinationAccount);
        creditTransaction.setUser(destinationAccount.getUser());
        transactionService.save(creditTransaction);

        return transactionId;
    }
}

/*
import jakarta.persistence.EntityNotFoundException;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.ai.AIAssistantService;
import ma.ensa.ebankingver1.ai.AIResponse;
import ma.ensa.ebankingver1.ai.WitAIClient;
import ma.ensa.ebankingver1.service.AuditService;
import ma.ensa.ebankingver1.service.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final BankAccountService bankAccountService;
    private final AIAssistantService aiAssistantService;
    private final AuditService auditService;
    private final WitAIClient witAIClient;

    @Autowired
    public ClientController(BankAccountService bankAccountService, AIAssistantService aiAssistantService,
                            AuditService auditService, WitAIClient witAIClient) {
        this.bankAccountService = bankAccountService;
        this.aiAssistantService = aiAssistantService;
        this.auditService = auditService;
        this.witAIClient = witAIClient;
    }


    @GetMapping("/{clientId}/accounts")
    public ResponseEntity<List<BankAccountDTO>> getClientAccounts(@PathVariable("clientId") String clientId) {
        Long id = parseClientId(clientId);
        List<BankAccountDTO> accounts = bankAccountService.getAccountsByClientId(id);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{clientId}/accounts/{accountId}")
    public ResponseEntity<BankAccountDTO> getClientAccount(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId) {
        BankAccountDTO account = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{clientId}/accounts/{accountId}/operations")
    public ResponseEntity<List<AccountOperationDTO>> getAccountOperations(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId) {
        List<AccountOperationDTO> operations = bankAccountService.accountHistory(accountId);
        return ResponseEntity.ok(operations);
    }

    @GetMapping("/{clientId}/accounts/operations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentTransactions(
            @PathVariable("clientId") String clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            if (page < 0 || size <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, null, "Invalid pagination parameters: page must be >= 0, size must be > 0"));
            }

            Long id = parseClientId(clientId);
            List<BankAccountDTO> accounts = bankAccountService.getAccountsByClientId(id);
            if (accounts == null || accounts.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("transactions", List.of());
                response.put("total", 0);
                response.put("page", page);
                response.put("size", size);
                response.put("totalPages", 0);
                return ResponseEntity.ok(new ApiResponse<>(true, response, null));
            }

            List<AccountOperationDTO> allOperations = accounts.stream()
                    .flatMap(account -> {
                        List<AccountOperationDTO> history = bankAccountService.accountHistory(account.getId());
                        return history != null ? history.stream() : Stream.empty();
                    })
                    .filter(operation -> operation.getOperationDate() != null)
                    .sorted((o1, o2) -> o2.getOperationDate().compareTo(o1.getOperationDate()))
                    .collect(Collectors.toList());

            int total = allOperations.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);
            List<AccountOperationDTO> paginatedOperations = (start < end) ? allOperations.subList(start, end) : List.of();

            Map<String, Object> response = new HashMap<>();
            response.put("transactions", paginatedOperations);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return ResponseEntity.ok(new ApiResponse<>(true, response, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to fetch transactions: " + e.getMessage()));
        }
    }
    @PostMapping(value = "/{clientId}/assistant", produces = "application/json")
    public ResponseEntity<AIResponse> interactWithAssistant(
            @PathVariable String clientId,
            @RequestBody String request,
            @RequestParam(defaultValue = "fr") String language) {
        try {
            logger.info("Processing AI request for clientId: {}, request: {}, language: {}", clientId, request, language);
            AIResponse response = aiAssistantService.processClientRequest(clientId, request, language);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in assistant endpoint for clientId: {}, request: {}. Exception: {}", clientId, request, e.getMessage(), e);
            return ResponseEntity.status(500).body(new AIResponse("Erreur serveur : " + e.getMessage(), false));
        }
    }

    private Long parseClientId(String clientId) {
        try {
            return Long.parseLong(clientId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid clientId: " + clientId);
        }
    }

    @PostMapping("/{clientId}/accounts/{accountId}/debit")
    public ResponseEntity<DebitDTO> debitAccount(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId, @RequestBody DebitDTO debitDTO) {
        try {
            bankAccountService.debit(accountId, debitDTO.getAmount(), debitDTO.getDescription());
            auditService.logAction("DEBIT_SUCCESS", "BANK_ACCOUNT", accountId,
                    Map.of("amount", debitDTO.getAmount(), "description", debitDTO.getDescription()), true);
            return ResponseEntity.ok(debitDTO);
        } catch (Exception e) {
            auditService.logAction("DEBIT_FAILED", "BANK_ACCOUNT", accountId,
                    Map.of("error", e.getMessage(), "amount", debitDTO.getAmount(), "description", debitDTO.getDescription()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/{clientId}/accounts/{accountId}/credit")
    public ResponseEntity<CreditDTO> creditAccount(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId, @RequestBody CreditDTO creditDTO) {
        try {
            bankAccountService.credit(accountId, creditDTO.getAmount(), creditDTO.getDescription());
            auditService.logAction("CREDIT_SUCCESS", "BANK_ACCOUNT", accountId,
                    Map.of("amount", creditDTO.getAmount(), "description", creditDTO.getDescription()), true);
            return ResponseEntity.ok(creditDTO);
        } catch (Exception e) {
            auditService.logAction("CREDIT_FAILED", "BANK_ACCOUNT", accountId,
                    Map.of("error", e.getMessage(), "amount", creditDTO.getAmount(), "description", creditDTO.getDescription()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/{clientId}/accounts/transfer")
    public ResponseEntity<ApiResponse<String>> transferFunds(@PathVariable("clientId") String clientId, @RequestBody TransferRequestDTO transferRequestDTO) {
        try {
            Long id = parseClientId(clientId);
            if (transferRequestDTO.getAmount() <= 0) {
                auditService.logAction("TRANSFER_REQUEST_FAILED", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                        Map.of("error", "Transfer amount must be positive", "destination", transferRequestDTO.getAccountDestination()), false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, null, "Error: Transfer amount must be positive"));
            }
            bankAccountService.transfer(transferRequestDTO.getAccountSource(), transferRequestDTO.getAccountDestination(), transferRequestDTO.getAmount());
            auditService.logAction("TRANSFER_REQUEST", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("amount", transferRequestDTO.getAmount(), "destination", transferRequestDTO.getAccountDestination()), true);
            return ResponseEntity.ok(new ApiResponse<>(true, "Transfert effectué avec succès.", null));
        } catch (EntityNotFoundException e) {
            auditService.logAction("TRANSFER_REQUEST_FAILED", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("error", e.getMessage(), "destination", transferRequestDTO.getAccountDestination()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            auditService.logAction("TRANSFER_REQUEST_FAILED", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("error", e.getMessage(), "destination", transferRequestDTO.getAccountDestination()), false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        } catch (IllegalStateException e) {
            auditService.logAction("TRANSFER_REQUEST_FAILED", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("error", e.getMessage(), "destination", transferRequestDTO.getAccountDestination()), false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        } catch (Exception e) {
            auditService.logAction("TRANSFER_REQUEST_FAILED", "BANK_ACCOUNT", transferRequestDTO.getAccountSource(),
                    Map.of("error", e.getMessage(), "destination", transferRequestDTO.getAccountDestination()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: An unexpected error occurred during the transfer: " + e.getMessage()));
        }
    }

    @GetMapping("/{clientId}/beneficiaries")
    public ResponseEntity<List<BeneficiaryDTO>> getBeneficiaries(@PathVariable("clientId") String clientId) {
        List<BeneficiaryDTO> beneficiaries = bankAccountService.getBeneficiaries(clientId);
        return ResponseEntity.ok(beneficiaries);
    }

    @PostMapping("/{clientId}/beneficiaries")
    public ResponseEntity<BeneficiaryDTO> addBeneficiary(@PathVariable("clientId") String clientId, @RequestBody BeneficiaryDTO beneficiaryDTO) {
        BeneficiaryDTO savedBeneficiary = bankAccountService.addBeneficiary(clientId, beneficiaryDTO);
        return ResponseEntity.ok(savedBeneficiary);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
    }
    @GetMapping("/assistant/health")
    public ResponseEntity<Map<String, Boolean>> checkAssistantHealth() {
        boolean isModelAvailable = witAIClient.testModelAvailability();
        Map<String, Boolean> response = new HashMap<>();
        response.put("modelAvailable", isModelAvailable);
        return ResponseEntity.ok(response);
    }
}
/*
@RestController
@RequestMapping("/api/employee")
public class ClientController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping(path = "/clients")
    public List<ClientDTO> Clients(){
        return bankAccountService.listClients();
    }
    @GetMapping(path = "/clients/search")
    public List<ClientDTO> searchClients(@RequestParam(name = "keyword" , defaultValue = "") String keyword){
        return bankAccountService.searchClients(keyword);
    }

    @GetMapping(path = "/clients/{id}")
    public ClientDTO getClient(@PathVariable(name = "id") Long clientid) throws Exception {
        return bankAccountService.getClient(clientid);
    }

    // signifie que les données du Client vont etre recuperer a partir des données de la requete en format JSON
    //SERA MODIFIE APRES LATIFAH WORK, it doesn't work here diff approach
    @PostMapping(path = "/clients")
    public ClientDTO saveClient(@RequestBody ClientDTO clientDTO){
        return bankAccountService.saveClient(clientDTO);
    }

    @PutMapping("/clients/{ClientId}")
    public ClientDTO updateClient(@PathVariable(name="ClientId") Long clientId,@RequestBody ClientDTO clientDTO){
        clientDTO.setId(clientId);
        return bankAccountService.updateClient(clientDTO);
    }

    @DeleteMapping(path = "/clients/{ClientId}")
    public void deleteClient(@PathVariable Long clientId){
        bankAccountService.deleteClient(clientId);
    }
}

 */



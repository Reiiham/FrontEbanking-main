package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;


    public ClientService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public List<ClientSummaryDTO> searchClientsByName(String name) {
        Role role = Role.CLIENT;
        List<User> users = userRepository.findUsersWithAccountsByFullName(role, name);
        return users.stream().map(ClientSummaryDTO::fromUser).toList();
    }

    public User activerServices(String clientId, List<String> services) {
        User user = userRepository.findById(Long.valueOf(clientId))
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        System.out.println("Client: " + user.getFirstName() + " " + user.getLastName());
        System.out.println("Compte bloqué ? " + user.getCompteBloque());
        System.out.println("Documents complets ? " + user.getDocumentsComplets());


        if (!isEligible(user)) {
            throw new RuntimeException("Client non éligible pour activation des services");
        }

        Set<String> servicesSet = new HashSet<>(user.getServicesActifs());
        servicesSet.addAll(services);

        user.setServicesActifs(new ArrayList<>(servicesSet));
        return userRepository.save(user);
    }

    private boolean isEligible(User client) {
        return Boolean.FALSE.equals(client.getCompteBloque()) && Boolean.TRUE.equals(client.getDocumentsComplets());
    }

    public List<ClientSummaryDTO> getClientsWithAccountsAndTransactions(String role) {
        List<User> users = userRepository.findUsersWithAccountsByRole(Role.CLIENT);

        // Test rapide pour debug
        System.out.println("Users found: " + users.size());
        for (User u : users) {
            System.out.println("User: " + u.getFirstName() + " " + u.getLastName() + ", Accounts: " + u.getAccounts().size());
        }

        //transformation en dto
        List<ClientSummaryDTO> dtos = users.stream()
                .map(user -> {
                    // Construire le nom complet
                    String fullName = user.getFirstName() + " " + user.getLastName();

                    // Convertir la liste des comptes User -> AccountSummaryDTO
                    List<AccountSummaryDTO> accountDTOs = user.getAccounts().stream()
                            .map(account -> {
                                AccountSummaryDTO accountDTO = new AccountSummaryDTO();
                                accountDTO.setAccountNumber(account.getId());
                                accountDTO.setAccountNumber(account.getAccountNumber());
                                accountDTO.setBalance(account.getBalance());
                                return accountDTO;
                            })
                            .collect(Collectors.toList());

                    // Construire et retourner le DTO client
                    return new ClientSummaryDTO(user.getId(), fullName, accountDTOs);
                })
                .collect(Collectors.toList());

        return dtos;
    }

//    public List<ClientSummaryDTO> searchClientsByName(String name) {
//        Role role = Role.CLIENT;
//        List<User> users = userRepository.findUsersWithAccountsByRoleAndNameContaining(role, name);
//        return users.stream().map(ClientSummaryDTO::fromUser).toList();
//    }

    public ClientSummaryDTO getClientWithDetails(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));

        return ClientSummaryDTO.fromUser(user);
    }

    public List<ClientBasicDTO> getAllClientsBasic() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(ClientBasicDTO::fromUser)
                .collect(Collectors.toList());
    }

    public void updateClientStatus(Long clientId, Boolean compteBloque, Boolean documentsComplets) {
        User user = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (compteBloque != null) {
            user.setCompteBloque(compteBloque);
        }
        if (documentsComplets != null) {
            user.setDocumentsComplets(documentsComplets);
        }

        userRepository.save(user);
    }

//    public List<AccountSummaryDTO> searchAccountsByRawNumber(String query) {
//        List<BankAccount> accounts = accountRepository.findByRawAccountNumberLike(query);
//        return accounts.stream().map(account -> {
//            AccountSummaryDTO dto = new AccountSummaryDTO();
//            dto.setAccountNumber(account.getRawAccountNumber()); // version lisible
//            dto.setType(account.getType());
//            dto.setBalance(account.getBalance());
//            return dto;
//        }).collect(Collectors.toList());
//    }





}
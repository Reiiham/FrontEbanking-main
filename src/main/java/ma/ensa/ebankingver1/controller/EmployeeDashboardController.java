package ma.ensa.ebankingver1.controller;
import ma.ensa.ebankingver1.DTO.ClientUpdateRequest;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.DTO.SecureActionRequest;
import ma.ensa.ebankingver1.DTO.ServiceSuspensionRequest;
import ma.ensa.ebankingver1.model.BankService;
import ma.ensa.ebankingver1.model.SuspendedService;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.SuspendedServiceRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import ma.ensa.ebankingver1.service.ClientService;
import ma.ensa.ebankingver1.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
public class EmployeeDashboardController {

    @Autowired
    private EnrollmentService service;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SuspendedServiceRepository suspendedServiceRepository;


    @GetMapping("/clients/count")
    public long countClients() {
        return service.countClients();
    }

    @GetMapping("/accounts/count")
    public long countAccounts() {
        return service.countAccounts();
    }

    // for the lists we have a simple list with just name and id à premiere vue (/clients/basic)
    //then when we click on the name we have //clients/{id} to display the client's details
    // then for the search bar it submits to /clients/search
    //and then if the employee wants the full list of clients with all of the details displayed at
    //once, it's /clients/detailed (idk if this is aesthetically pleasing tho, u choose)


    //endpoint for the simple list of all clients
    @GetMapping("/clients/basic")
    public List<ClientBasicDTO> listClientsBasic() {
        return clientService.getAllClientsBasic();
    }

    //endpoint for the list of all clients with details
    @GetMapping("/clients/detailed")
    public List<ClientSummaryDTO> listClients(@RequestParam(name = "search", required = false) String search) {
        return clientService.getClientsWithAccountsAndTransactions(search);
    }

    //endpoint to search client by name
    @GetMapping("/clients/search")
    public List<ClientSummaryDTO> searchClientsByName(@RequestParam("name") String name) {
        return clientService.searchClientsByName(name);
    }

//    //recherche par numeros de comptes
//    @GetMapping("/accounts/search")
//    public List<AccountSummaryDTO> searchAccounts(@RequestParam("query") String query) {
//        return clientService.searchAccountsByRawNumber(query);
//    }

    //endpoint pour afficher les details d'un client
    @GetMapping("/clients/{id}")
    public ClientSummaryDTO getClientDetails(@PathVariable("id") Long id) {
        return clientService.getClientWithDetails(id);
    }

    @PostMapping("/enroll")
    public void enroll(@RequestBody EnrollmentRequest req) {
        service.enrollClient(req);
    }

//    @PutMapping("/update")
//    public void update(@RequestBody ClientUpdateRequest req) {
//        boolean ok = service.updateClient(req);
//        if (!ok) {
//            throw new RuntimeException("Update failed: invalid supervisor password or client not found.");
//        }
//    }

    //update now requires a supervisor pw
    @PutMapping("/update")
    public ResponseEntity<String> update(@RequestBody ClientUpdateRequest req) {
        if (!service.validateSupervisor(req.getSupervisorCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code superviseur incorrect.");
        }
        boolean ok = service.updateClient(req);
        if (!ok) {
            return ResponseEntity.badRequest().body("Échec de la mise à jour : client introuvable ou données invalides.");
        }
        return ResponseEntity.ok("Client mis à jour avec succès.");
    }


//    @DeleteMapping("/delete")
//    public void delete(@RequestBody ClientDeletionRequest req) {
//        boolean ok = service.deleteClient(req.getClientId());
//        if (!ok) {
//            throw new RuntimeException("Deletion failed: client has active transactions or does not exist.");
//        }
//    }

    // on a changé le endpoint pour delete, maintenant, it require a supervisor password
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody SecureActionRequest req) {
        if (!service.validateSupervisor(req.getSupervisorCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code superviseur incorrect.");
        }
        boolean ok = service.deleteClient(req.getClientId());
        if (!ok) {
            return ResponseEntity.badRequest().body("Échec de la suppression : client introuvable ou a des transactions actives.");
        }
        return ResponseEntity.ok("Client supprimé avec succès.");
    }

    // Endpoint pour toggle compteBloque et documentsComplets
    @PutMapping("/clients/{id}/status")
    public ResponseEntity<String> updateClientStatus(
            @PathVariable("id") Long id,
            @RequestParam("compteBloque") Boolean compteBloque,
            @RequestParam("documentsComplets") Boolean documentsComplets) {

        try {
            clientService.updateClientStatus(id, compteBloque, documentsComplets);
            return ResponseEntity.ok("Statuts mis à jour avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //endpoint pour activer un service donné
    //exemple d'entree
//    {
//        "clientId": "3",
//            "services": ["VIREMENT", "CHEQUIER"]
//    }
    //NB il faut que l'admin ajoute la liste des services ou bien que ca soit un enum
    @PostMapping("/clients/activer-services")
    public ResponseEntity<?> activerServicesPourClient(@RequestBody ActivateServicesRequest request) {
        try {
            User updatedUser = clientService.activerServices(request.getClientId(), request.getServices());
            return ResponseEntity.ok("Services activés avec succès pour " + updatedUser.getFirstName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //endpoint pour la suspension d'un client
    //BankService est un enum donc en front on aura une liste dépliante to select from
    //exemple d'entree:
//    {
//        "clientId": 1,
//            "servicesToSuspend": ["VIREMENT", "CHEQUIER"],
//        "reason": "Non-paiement",
//            "notificationMessage": "Vos services sont suspendus pour non-paiement"
//    }
    @PutMapping("/clients/{id}/suspend-services")
    public ResponseEntity<String> suspendServices(
            @PathVariable("id") Long id,
            @RequestBody ServiceSuspensionRequest request) {

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = userOptional.get();

        for (BankService service : request.getServicesToSuspend()) {
            SuspendedService suspended = new SuspendedService();
            suspended.setServiceName(service.name()); // On enregistre le nom de l'enum en base
            suspended.setReason(request.getReason());
            suspended.setNotificationMessage(request.getNotificationMessage());
            suspended.setUser(user);

            user.getSuspendedServices().add(suspended);
        }

        userRepository.save(user);

        return ResponseEntity.ok("Services suspendus avec succès");
    }


    //réactive un service desactivé
    //   exemple d'entree:
    //["VIREMENT", "CHEQUIER"]
    @PutMapping("/clients/{id}/reactivate-services")
    public ResponseEntity<String> reactivateServices(
            @PathVariable("id") Long id,
            @RequestBody List<String> servicesToReactivate) {

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = userOptional.get();

        // Convertir les chaînes en enums de manière sûre
        List<BankService> validServices = servicesToReactivate.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter(name -> {
                    try {
                        BankService.valueOf(name);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(BankService::valueOf)
                .toList();

        List<SuspendedService> currentSuspended = user.getSuspendedServices();
        List<SuspendedService> toRemove = currentSuspended.stream()
                .filter(s -> validServices.contains(s.getServiceName()))
                .toList();

        currentSuspended.removeAll(toRemove);
        suspendedServiceRepository.deleteAll(toRemove);

        userRepository.save(user);

        return ResponseEntity.ok("Services réactivés avec succès");
    }
}

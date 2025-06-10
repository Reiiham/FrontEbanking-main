package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuspensionService {

    private final UserRepository userRepository;
    private final SuspendedServiceRepository suspendedServiceRepository;

    public SuspensionService(UserRepository userRepository, SuspendedServiceRepository suspendedServiceRepository) {
        this.userRepository = userRepository;
        this.suspendedServiceRepository = suspendedServiceRepository;
    }
}

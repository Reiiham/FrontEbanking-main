package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.CreateBeneficiaryRequest;
import ma.ensa.ebankingver1.DTO.UpdateBeneficiaryRequest;
import ma.ensa.ebankingver1.model.Beneficiary;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.BeneficiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiaryService {
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    public List<Beneficiary> findByUserIdAndActif(Long userId, Boolean actif) {
        if (actif == null) {
            return beneficiaryRepository.findByUserId(userId);
        }
        return beneficiaryRepository.findByUserIdAndActif(userId, actif);
    }

    public boolean existsByUserIdAndRib(Long userId, String rib) {
        return beneficiaryRepository.existsByUserIdAndRib(userId, rib);
    }

    public Beneficiary createBeneficiary(User user, CreateBeneficiaryRequest request) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUser(user);
        beneficiary.setNom(request.getNom());
        beneficiary.setPrenom(request.getPrenom());
        beneficiary.setRib(request.getRib());
        beneficiary.setRelation(request.getRelation());
        beneficiary.setSurnom(request.getSurnom());

        return beneficiaryRepository.save(beneficiary);
    }

    public Beneficiary findByIdAndUserId(Long id, Long userId) {
        return beneficiaryRepository.findByIdAndUserId(id, userId).orElse(null);
    }

    public Beneficiary updateBeneficiary(Beneficiary beneficiary, UpdateBeneficiaryRequest request) {
        if (request.getNom() != null) beneficiary.setNom(request.getNom());
        if (request.getPrenom() != null) beneficiary.setPrenom(request.getPrenom());
        if (request.getRelation() != null) beneficiary.setRelation(request.getRelation());
        if (request.getSurnom() != null) beneficiary.setSurnom(request.getSurnom());
        if (request.getActif() != null) beneficiary.setActif(request.getActif());

        return beneficiaryRepository.save(beneficiary);
    }

    public void deactivateBeneficiary(Beneficiary beneficiary) {
        beneficiary.setActif(false);
        beneficiaryRepository.save(beneficiary);
    }

    public List<Beneficiary> searchBeneficiaries(Long userId, String query) {
        return beneficiaryRepository.searchByUserIdAndName(userId, query);
    }
}
package ma.ensa.ebankingver1.DTO;

import java.time.LocalDateTime;

import ma.ensa.ebankingver1.model.Beneficiary;
import ma.ensa.ebankingver1.model.RelationType;

// DTO pour la réponse
public class BeneficiaryResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String nomComplet;
    private String nomAffichage;
    private String rib;
    private RelationType relation;
    private String relationDisplayName;
    private String surnom;
    private LocalDateTime dateCreation;
    private Boolean actif;

    // Constructeur à partir de l'entité
    public BeneficiaryResponseDTO(Beneficiary beneficiary) {
        this.id = beneficiary.getId();
        this.nom = beneficiary.getNom();
        this.prenom = beneficiary.getPrenom();
        this.nomComplet = beneficiary.getNomComplet();
        this.nomAffichage = beneficiary.getNomAffichage();
        this.rib = beneficiary.getRib();
        this.relation = beneficiary.getRelation();
        this.relationDisplayName = beneficiary.getRelation().getDisplayName();
        this.surnom = beneficiary.getSurnom();
        this.dateCreation = beneficiary.getDateCreation();
        this.actif = beneficiary.getActif();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getNomAffichage() { return nomAffichage; }
    public void setNomAffichage(String nomAffichage) { this.nomAffichage = nomAffichage; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public RelationType getRelation() { return relation; }
    public void setRelation(RelationType relation) { this.relation = relation; }

    public String getRelationDisplayName() { return relationDisplayName; }
    public void setRelationDisplayName(String relationDisplayName) { this.relationDisplayName = relationDisplayName; }


    public String getSurnom() { return surnom; }
    public void setSurnom(String surnom) { this.surnom = surnom; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}
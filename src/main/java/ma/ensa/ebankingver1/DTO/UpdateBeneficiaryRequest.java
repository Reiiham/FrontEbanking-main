package ma.ensa.ebankingver1.DTO;



import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.*;
import ma.ensa.ebankingver1.model.RelationType;
// DTO pour mettre à jour un bénéficiaire
public class UpdateBeneficiaryRequest {
    private String nom;
    private String prenom;
    private RelationType relation;
    private String surnom;
    private Boolean actif;

    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public RelationType getRelation() { return relation; }
    public void setRelation(RelationType relation) { this.relation = relation; }


    public String getSurnom() { return surnom; }
    public void setSurnom(String surnom) { this.surnom = surnom; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}


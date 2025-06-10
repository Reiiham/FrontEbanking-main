package ma.ensa.ebankingver1.DTO;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.*;

import ma.ensa.ebankingver1.model.RelationType;
// DTO pour créer un bénéficiaire
public class CreateBeneficiaryRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String prenom;

    @NotBlank(message = "Le RIB est obligatoire")
   // @Pattern(regexp = "^[0-9]{24}$", message = "Le RIB doit contenir 24 chiffres")
    private String rib;

    @NotNull(message = "La relation est obligatoire")
    private RelationType relation;


    private String surnom;

    // Constructeurs
    public CreateBeneficiaryRequest() {}

    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public RelationType getRelation() { return relation; }
    public void setRelation(RelationType relation) { this.relation = relation; }


    public String getSurnom() { return surnom; }
    public void setSurnom(String surnom) { this.surnom = surnom; }
}

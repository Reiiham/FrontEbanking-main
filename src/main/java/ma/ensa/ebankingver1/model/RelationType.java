package ma.ensa.ebankingver1.model;

public enum RelationType {
    FAMILLE("Famille"),
    AMI("Ami(e)"),
    CONJOINT("Conjoint(e)"),
    ENFANT("Enfant"),
    PARENT("Parent"),
    FRERE_SOEUR("Frère/Sœur"),
    COLLEGUE("Collègue"),
    FOURNISSEUR("Fournisseur"),
    CLIENT("Client"),
    AUTRE("Autre");

    private final String displayName;

    RelationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
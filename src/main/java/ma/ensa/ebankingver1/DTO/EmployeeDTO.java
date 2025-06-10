package ma.ensa.ebankingver1.DTO;

import ma.ensa.ebankingver1.model.Role;
import jakarta.validation.constraints.NotBlank;

public class EmployeeDTO {

    @NotBlank(message = "L'email est obligatoire")
    private String emailPersonnel;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;

    @NotBlank(message = "Le CIN est obligatoire")
    private String cin;


    //private String cinPhotoFilename;
    // Constructeurs
    public EmployeeDTO() {}

    public EmployeeDTO(String firstName, String lastName, String phoneNumber,
                       String cin, String emailPersonnel, String cinPhoto) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.cin = cin;
        this.emailPersonnel = emailPersonnel;
        //this.cinPhotoFilename = cinPhoto;
    }

    private Role role;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

/*blic String getCinPhotoFilename() {
        return cinPhotoFilename;
    }
    public void setCinPhotoFilename(String cinPhotoFilename) {
        this.cinPhotoFilename = cinPhotoFilename;
    }

 */

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    public String getEmailPersonnel() {
        return emailPersonnel;
    }
    public void setEmailPersonnel(String emailPersonnel) {
        this.emailPersonnel = emailPersonnel;
    }
}
package ma.ensa.ebankingver1.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    //setters and getters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @JsonProperty("name")
    public String getName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}



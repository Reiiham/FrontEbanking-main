package ma.ensa.ebankingver1.DTO;

import ma.ensa.ebankingver1.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ClientSummaryDTO {
    private Long clientId;
    private String fullName;
    private List<AccountSummaryDTO> accounts;
    private Boolean compteBloque;
    private Boolean documentsComplets;

    // constructeurs, getters/setters
    public ClientSummaryDTO() {
        super();
    }
    public ClientSummaryDTO(Long clientId, String fullName, List<AccountSummaryDTO> accounts) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.accounts = accounts;
    }

    public static ClientSummaryDTO fromUser(User user) {
        ClientSummaryDTO dto = new ClientSummaryDTO();

        dto.setClientId(user.getId());
        dto.setFullName(user.getFirstName() + " " + user.getLastName());

        dto.setAccounts(
                user.getAccounts().stream().map(account -> {
                    AccountSummaryDTO accDto = new AccountSummaryDTO();
                    accDto.setAccountNumber(account.getAccountNumber());
                    accDto.setType(account.getType());
                    accDto.setBalance(account.getBalance());

                    accDto.setTransactions(
                            account.getTransactions().stream().map(tx -> {
                                TransactionDTO txDto = new TransactionDTO();
                                txDto.setTransactionId(tx.getId()); // id est un String
                                txDto.setAmount(tx.getAmount());
                                txDto.setDate(tx.getDate()); // date est LocalDateTime
                                return txDto;
                            }).collect(Collectors.toList())
                    );

                    return accDto;
                }).collect(Collectors.toList())
        );

        return dto;
    }


    public Long getClientId() {
        return clientId;
    }
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public List<AccountSummaryDTO> getAccounts() {
        return accounts;
    }
    public void setAccounts(List<AccountSummaryDTO> accounts) {
        this.accounts = accounts;
    }

    public Boolean getCompteBloque() {
        return compteBloque;
    }
    public void setCompteBloque(Boolean compteBloque) {
        this.compteBloque = compteBloque;
    }
    public Boolean getDocumentsComplets() {
        return documentsComplets;
    }
    public void setDocumentsComplets(Boolean documentsComplets) {
        this.documentsComplets = documentsComplets;
    }
}

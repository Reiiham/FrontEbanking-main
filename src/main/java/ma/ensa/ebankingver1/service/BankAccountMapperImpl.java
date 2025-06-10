package ma.ensa.ebankingver1.service;
/*
import ma.ensa.ebankingver1.DTO.AccountOperationDTO;
import ma.ensa.ebankingver1.DTO.ClientDTO;
import ma.ensa.ebankingver1.DTO.CurrentBankAccountDTO;
import ma.ensa.ebankingver1.DTO.SavingBankAccountDTO;
import ma.ensa.ebankingver1.model.AccountOperation;
import ma.ensa.ebankingver1.model.CurrentAccount;
import ma.ensa.ebankingver1.model.SavingAccount;
import ma.ensa.ebankingver1.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BankAccountMapperImpl {
    public ClientDTO fromClient(User client) {
        ClientDTO clientDTO = new ClientDTO();
        BeanUtils.copyProperties(client, clientDTO);
        return clientDTO;
    }

    public User fromClientDTO(ClientDTO clientDTO) {
        User client = new User();
        BeanUtils.copyProperties(clientDTO, client);
        return client;
    }

    public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount) {
        SavingBankAccountDTO dto = new SavingBankAccountDTO();
        BeanUtils.copyProperties(savingAccount, dto);
        dto.setClient(fromClient(savingAccount.getClient()));
        dto.setType(savingAccount.getClass().getSimpleName());
        return dto;
    }

    public SavingAccount fromSavingAccountBankAccountDTO(SavingBankAccountDTO dto) {
        SavingAccount savingAccount = new SavingAccount();
        BeanUtils.copyProperties(dto, savingAccount);
        savingAccount.setClient(fromClientDTO(dto.getClient()));
        return savingAccount;
    }

    public CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentAccount) {
        CurrentBankAccountDTO dto = new CurrentBankAccountDTO();
        BeanUtils.copyProperties(currentAccount, dto);
        dto.setClient(fromClient(currentAccount.getClient()));
        dto.setType(currentAccount.getClass().getSimpleName());
        return dto;
    }

    public CurrentAccount fromCurrentAccountBankAccountDTO(CurrentBankAccountDTO dto) {
        CurrentAccount currentAccount = new CurrentAccount();
        BeanUtils.copyProperties(dto, currentAccount);
        currentAccount.setClient(fromClientDTO(dto.getClient()));
        return currentAccount;
    }

    public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation) {
        AccountOperationDTO dto = new AccountOperationDTO();
        BeanUtils.copyProperties(accountOperation, dto);
        return dto;
    }
}

 */
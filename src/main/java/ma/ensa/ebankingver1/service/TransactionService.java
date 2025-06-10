package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {
    Transaction save(Transaction transaction);
    List<Transaction> findTransfersByUserId(Long userId, int page, int size);
    List<Transaction> findByAccountId(String accountId);
}

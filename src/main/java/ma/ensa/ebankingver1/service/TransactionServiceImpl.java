package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.Transaction;
import ma.ensa.ebankingver1.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findTransfersByUserId(Long clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionRepository.findTransfersByUserId(clientId, pageable);
        return transactionPage.getContent(); // Retourner le contenu de la page
    }
    @Override
    public List<Transaction> findByAccountId(String accountId) {
        return transactionRepository.findByAccountIdOrderByDateDesc(accountId);
    }
}
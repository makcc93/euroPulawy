package pl.eurokawa.transaction;

import java.math.BigDecimal;
import java.util.List;

public class TransactionServiceImpl implements TransactionService{
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Transaction> findAllSavedNotConfirmedTransactions() {
        return transactionRepository.findAllSavedNotConfirmedTransactions();
    }

    @Override
    public List<Transaction> findAllConfirmedTransactions() {
        return transactionRepository.findAllConfirmedTransactions();
    }

    @Override
    public List<Transaction> findAllUserConfirmedTransactions(Integer userId) {
        return transactionRepository.findAllUserConfirmedTransactions(userId);
    }

    @Override
    public BigDecimal getSumOfUserDeposit(Integer userId) {
        return transactionRepository.getSumOfUserDeposit(userId);
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        transactionRepository.delete(transaction);
    }
}

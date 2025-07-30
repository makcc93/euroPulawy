package pl.eurokawa.transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    List<Transaction> findAllSavedNotConfirmedTransactions();
    List<Transaction> findAllConfirmedTransactions();
    List<Transaction> findAllUserConfirmedTransactions(Integer userId);
    BigDecimal getSumOfUserDeposit(Integer userId);
    Transaction save(Transaction transaction);
    void delete(Transaction transaction);
}

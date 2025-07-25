package pl.eurokawa.balance;


import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;

public interface BalanceService {
    void updateBalance(User user, BigDecimal value, TransactionType transactionType);
    BigDecimal getCurrentBalance();
}

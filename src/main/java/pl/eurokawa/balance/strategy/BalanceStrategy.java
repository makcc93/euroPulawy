package pl.eurokawa.balance.strategy;

import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

public interface BalanceStrategy {
    boolean supports(TransactionType transactionType);
    BigDecimal generateNewBalanceValue(BigDecimal currentBalance, BigDecimal transactionValue);
}

package pl.eurokawa.balance.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

@Component
public class DepositBalanceStrategy implements BalanceStrategy{
    @Override
    public boolean supports(TransactionType transactionType) {
        TransactionTypeNullChecker.check(transactionType);

        return transactionType == TransactionType.DEPOSIT;
    }

    @Override
    public BigDecimal generateNewBalanceValue(BigDecimal currentBalance, BigDecimal transactionValue) {
        BigDecimalNullChecker.check(currentBalance,transactionValue);
        NegativeValueChecker.check(transactionValue);

        return currentBalance.add(transactionValue);
    }
}

package pl.eurokawa.balance.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

@Component
public class ManualBalanceStrategy implements BalanceStrategy{
    @Override
    public boolean supports(TransactionType transactionType) {
        TransactionTypeNullChecker.check(transactionType);

        return transactionType == TransactionType.MANUAL;
    }

    @Override
    public BigDecimal generateNewBalanceValue(BigDecimal currentBalance, BigDecimal transactionValue) {
        BigDecimalNullChecker.check(currentBalance,transactionValue);

        return transactionValue;
    }
}

package pl.eurokawa.balance.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

@Component
public class ManualBalanceStrategy implements BalanceStrategy{
    @Override
    public boolean supports(TransactionType transactionType) {
        ArgumentNullChecker.check(transactionType);

        return transactionType == TransactionType.MANUAL;
    }

    @Override
    public BigDecimal generateNewBalanceValue(BigDecimal currentBalance, BigDecimal transactionValue) {
        ArgumentNullChecker.check(currentBalance,"Current balance");
        ArgumentNullChecker.check(transactionValue,"Transaction value");

        return transactionValue;
    }
}

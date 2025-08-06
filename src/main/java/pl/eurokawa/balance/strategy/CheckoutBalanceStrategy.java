package pl.eurokawa.balance.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

@Component
public class CheckoutBalanceStrategy implements BalanceStrategy{
    @Override
    public boolean supports(TransactionType transactionType) {
        ArgumentNullChecker.check(transactionType, "Transaction type");

        return transactionType == TransactionType.CHECKOUT;
    }

    @Override
    public BigDecimal generateNewBalanceValue(BigDecimal currentBalance, BigDecimal transactionValue) {
        ArgumentNullChecker.check(currentBalance,"Current balance");
        ArgumentNullChecker.check(transactionValue,"Transaction value");
        NegativeValueChecker.check(transactionValue);

        return currentBalance.subtract(transactionValue);
    }
}

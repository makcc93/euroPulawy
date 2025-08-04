package pl.eurokawa.balance.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

@Component
public class CheckoutBalanceStrategy implements BalanceStrategy{
    @Override
    public boolean supports(TransactionType transactionType) {
        return transactionType == TransactionType.CHECKOUT;
    }

    @Override
    public BigDecimal generateNewBalanceValue(BigDecimal currentBalance, BigDecimal transactionValue) {
        BigDecimalNullChecker.check(currentBalance,transactionValue);
        NegativeValueChecker.check(transactionValue);

        return currentBalance.subtract(transactionValue);
    }
}

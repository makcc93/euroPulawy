package pl.eurokawa.balance.strategy;

import java.math.BigDecimal;

public class NegativeValueChecker {
    public static void check(BigDecimal transactionValue){
        if (transactionValue.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Transaction value cannot be negative");
        }
    }
}

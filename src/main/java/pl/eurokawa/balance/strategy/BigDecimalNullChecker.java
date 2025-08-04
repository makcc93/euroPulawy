package pl.eurokawa.balance.strategy;

import java.math.BigDecimal;
import java.util.Objects;

public class InputNullChecker {
    public static void check(BigDecimal currentBalance, BigDecimal transactionValue){
        Objects.requireNonNull(currentBalance,"Current balance value cannot be null");
        Objects.requireNonNull(transactionValue,"Transaction value cannot be null");
    }
}

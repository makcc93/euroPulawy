package pl.eurokawa.balance.strategy;

import pl.eurokawa.transaction.TransactionType;

import java.util.Objects;

public class TransactionTypeNullChecker {
    public static void check(TransactionType transactionType){
        Objects.requireNonNull(transactionType);
    }
}

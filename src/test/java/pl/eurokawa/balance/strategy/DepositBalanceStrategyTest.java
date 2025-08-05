package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DepositBalanceStrategyTest {

    @Test
    void supportsTest(){
        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();
        boolean supports = depositBalanceStrategy.supports(TransactionType.DEPOSIT);

        assertTrue(supports);
    }

    @Test
    void supportsTransactionTypeIsNull(){
        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

       assertThrows(NullPointerException.class, () -> depositBalanceStrategy.supports(null));
    }

    @Test
    void generateNewBalanceValueCheck(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositValue = new BigDecimal("20.00");

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();
        BigDecimal expectedNewValue = depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue);

        assertEquals(new BigDecimal("120.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValueCheckNegativeValue(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositValue = new BigDecimal("-20.00");

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be negative",exception.getMessage());
    }

    @Test
    void generateNewBalanceValueCurrentBalanceIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal depositValue = new BigDecimal("10.00");

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Current balance value cannot be null", exception.getMessage());
    }

    @Test
    void generateNewBalanceValueTransactionValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("10.00");
        BigDecimal depositValue = null;

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be null", exception.getMessage());
    }
}

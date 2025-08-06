package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DepositBalanceStrategyTest {

    @Test
    void supports_WorkingTest(){
        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();
        boolean supports = depositBalanceStrategy.supports(TransactionType.DEPOSIT);

        assertTrue(supports);
    }

    @Test
    void supports_TransactionTypeIsNull(){
        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

       assertThrows(NullPointerException.class, () -> depositBalanceStrategy.supports(null));
    }

    @Test
    void generateNewBalanceValue_WorkingTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositValue = new BigDecimal("20.00");
        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();
        BigDecimal expectedNewValue = depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue);

        assertEquals(new BigDecimal("120.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValue_DepositHasNegativeValue(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositValue = new BigDecimal("-20.00");

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be negative",exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_CurrentBalanceIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal depositValue = new BigDecimal("10.00");

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Current balance cannot be null", exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_TransactionValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("10.00");
        BigDecimal depositValue = null;

        DepositBalanceStrategy depositBalanceStrategy = new DepositBalanceStrategy();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> depositBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be null", exception.getMessage());
    }
}

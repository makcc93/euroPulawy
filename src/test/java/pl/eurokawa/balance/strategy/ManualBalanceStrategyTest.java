package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ManualBalanceStrategyTest {

    @Test
    void supportsTest(){
        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        boolean supports = manualBalanceStrategy.supports(TransactionType.MANUAL);

        assertTrue(supports);
    }

    @Test
    void supportsTransactionTypeIsNull(){
        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();

        assertThrows(NullPointerException.class, () -> manualBalanceStrategy.supports(null));
    }

    @Test
    void generateNewBalanceValueTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal manualValue = new BigDecimal("50.00");

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        BigDecimal expectedNewValue = manualBalanceStrategy.generateNewBalanceValue(currentBalance, manualValue);

        assertEquals(new BigDecimal("50.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValueManualValueIsNegative(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal manualValue = new BigDecimal("-66.00");

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        BigDecimal expectedNewValue = manualBalanceStrategy.generateNewBalanceValue(currentBalance, manualValue);

        assertEquals(new BigDecimal("-66.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValueCurrentValueIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal manualValue = new BigDecimal("10.00");

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> manualBalanceStrategy.generateNewBalanceValue(currentBalance, manualValue));

        assertEquals("Current balance value cannot be null", exception.getMessage());
    }

    @Test
    void generateNewBalanceValueManualValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("10.00");
        BigDecimal depositValue = null;

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> manualBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be null", exception.getMessage());
    }
}

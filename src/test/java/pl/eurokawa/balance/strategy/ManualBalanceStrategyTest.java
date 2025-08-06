package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ManualBalanceStrategyTest {

    @Test
    void supports_WorkingTest(){
        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        boolean supports = manualBalanceStrategy.supports(TransactionType.MANUAL);

        assertTrue(supports);
    }

    @Test
    void supports_TransactionTypeIsNull(){
        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();

        assertThrows(NullPointerException.class, () -> manualBalanceStrategy.supports(null));
    }

    @Test
    void generateNewBalanceValue_WorkingTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal manualValue = new BigDecimal("50.00");

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        BigDecimal expectedNewValue = manualBalanceStrategy.generateNewBalanceValue(currentBalance, manualValue);

        assertEquals(new BigDecimal("50.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValue_ManualValueIsNegative(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal manualValue = new BigDecimal("-66.00");

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        BigDecimal expectedNewValue = manualBalanceStrategy.generateNewBalanceValue(currentBalance, manualValue);

        assertEquals(new BigDecimal("-66.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValue_CurrentValueIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal manualValue = new BigDecimal("10.00");

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> manualBalanceStrategy.generateNewBalanceValue(currentBalance, manualValue));

        assertEquals("Current balance cannot be null", exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_ManualValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("10.00");
        BigDecimal depositValue = null;

        ManualBalanceStrategy manualBalanceStrategy = new ManualBalanceStrategy();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> manualBalanceStrategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be null", exception.getMessage());
    }
}

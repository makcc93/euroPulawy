package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ManualBalanceStrategyTest {

    @InjectMocks
    ManualBalanceStrategy strategy;

    @Test
    void supports_WorkingTest(){
        boolean supports = strategy.supports(TransactionType.MANUAL);

        assertTrue(supports);
    }

    @Test
    void supports_TransactionTypeIsNull(){
        assertThrows(NullPointerException.class, () -> strategy.supports(null));
    }

    @Test
    void generateNewBalanceValue_WorkingTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal manualValue = new BigDecimal("50.00");

        BigDecimal expectedNewValue = strategy.generateNewBalanceValue(currentBalance, manualValue);

        assertEquals(new BigDecimal("50.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValue_ManualValueIsNegative(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal manualValue = new BigDecimal("-66.00");

        BigDecimal expectedNewValue = strategy.generateNewBalanceValue(currentBalance, manualValue);

        assertEquals(new BigDecimal("-66.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValue_CurrentValueIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal manualValue = new BigDecimal("10.00");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> strategy.generateNewBalanceValue(currentBalance, manualValue));

        assertEquals("Current balance cannot be null", exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_ManualValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("10.00");
        BigDecimal depositValue = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> strategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be null", exception.getMessage());
    }
}

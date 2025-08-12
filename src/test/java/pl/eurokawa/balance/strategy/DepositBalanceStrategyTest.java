package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DepositBalanceStrategyTest {

    @InjectMocks
    DepositBalanceStrategy strategy;

    @Test
    void supports_WorkingTest(){
        boolean supports = strategy.supports(TransactionType.DEPOSIT);

        assertTrue(supports);
    }

    @Test
    void supports_CheckoutShouldNotPassed(){
        assertFalse(strategy.supports(TransactionType.CHECKOUT));
    }

    @Test
    void supports_TransactionTypeIsNull(){
       assertThrows(NullPointerException.class, () -> strategy.supports(null));
    }

    @Test
    void generateNewBalanceValue_WorkingTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositValue = new BigDecimal("20.00");
        BigDecimal expectedNewValue = strategy.generateNewBalanceValue(currentBalance, depositValue);

        assertEquals(new BigDecimal("120.00"),expectedNewValue);
    }

    @Test
    void generateNewBalanceValue_DepositHasNegativeValue(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositValue = new BigDecimal("-20.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> strategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be negative",exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_CurrentBalanceIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal depositValue = new BigDecimal("10.00");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> strategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Current balance cannot be null", exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_TransactionValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("10.00");
        BigDecimal depositValue = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> strategy.generateNewBalanceValue(currentBalance, depositValue));

        assertEquals("Transaction value cannot be null", exception.getMessage());
    }
}

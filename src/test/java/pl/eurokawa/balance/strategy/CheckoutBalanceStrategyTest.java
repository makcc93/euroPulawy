package pl.eurokawa.balance.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.transaction.TransactionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CheckoutBalanceStrategyTest {

    @Test
    void supports_WorkingTest(){
        CheckoutBalanceStrategy checkoutBalanceStrategy = new CheckoutBalanceStrategy();
        boolean expectCheckout = checkoutBalanceStrategy.supports(TransactionType.CHECKOUT);

        assertTrue(expectCheckout);
    }

    @Test
    void supports_TransactionTypeIsNull(){
        CheckoutBalanceStrategy checkoutBalanceStrategy = new CheckoutBalanceStrategy();
        assertThrows(NullPointerException.class, () -> checkoutBalanceStrategy.supports(null));
    }

    @Test
    void generateNewBalanceValue_WorkingTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal checkoutValue = new BigDecimal("20.00");

        CheckoutBalanceStrategy checkoutBalanceStrategy = new CheckoutBalanceStrategy();
        BigDecimal result = checkoutBalanceStrategy.generateNewBalanceValue(currentBalance, checkoutValue);

        assertEquals(new BigDecimal("80.00"),result);
    }

    @Test
    void generateNewBalanceValue_CurrentBalanceIsNull(){
        BigDecimal currentBalance = null;
        BigDecimal checkoutValue = new BigDecimal("20.00");

        CheckoutBalanceStrategy checkoutBalanceStrategy = new CheckoutBalanceStrategy();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> checkoutBalanceStrategy.generateNewBalanceValue(currentBalance, checkoutValue));

        assertEquals("Current balance cannot be null",exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_CheckoutValueIsNull(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal checkoutValue = null;

        CheckoutBalanceStrategy checkoutBalanceStrategy = new CheckoutBalanceStrategy();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> checkoutBalanceStrategy.generateNewBalanceValue(currentBalance, checkoutValue));

        assertEquals("Transaction value cannot be null",exception.getMessage());
    }

    @Test
    void generateNewBalanceValue_NegativeCheckoutValue(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal negativeCheckoutValue = new BigDecimal("-20.00");

        CheckoutBalanceStrategy checkoutBalanceStrategy = new CheckoutBalanceStrategy();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> checkoutBalanceStrategy.generateNewBalanceValue(currentBalance, negativeCheckoutValue));

        assertEquals("Transaction value cannot be negative",exception.getMessage());
    }
}
